package oxchains.fabric.sdk;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.RevokeReason;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import oxchains.fabric.console.data.ChainRepo;
import oxchains.fabric.console.domain.ChainCodeInfo;
import oxchains.fabric.console.domain.ChainInfo;
import oxchains.fabric.console.domain.PeerEventhub;
import oxchains.fabric.console.rest.common.RestResp;
import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.LongStream;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.*;
import static java.util.Optional.empty;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static org.hyperledger.fabric.sdk.ChaincodeResponse.Status.SUCCESS;
import static org.hyperledger.fabric.sdk.TransactionRequest.Type.GO_LANG;
import static org.hyperledger.fabric.sdk.TransactionRequest.Type.JAVA;
import static org.hyperledger.fabric.sdk.security.CryptoSuite.Factory.getCryptoSuite;
import static org.hyperledger.fabric_ca.sdk.RevokeReason.UNSPECIFIED;

/**
 * @author aiet
 */
@Component
public class FabricSDK {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final ChainRepo chainRepo;
    private final String[] EMPTY_ARGS = new String[] {};
    public FabricSDK(@Autowired ChainRepo chainRepo) {
        this.chainRepo = chainRepo;
    }
    private final WeakHashMap<String, Peer> PEER_CACHE = new WeakHashMap<>(8);
    private final WeakHashMap<String, EventHub> EVENTHUB_CACHE = new WeakHashMap<>(8);
    private final WeakHashMap<String, ChaincodeID> CHAINCODE_CACHE = new WeakHashMap<>(8);
    private final Map<String, HFCAClient> CA_CLIENTS = synchronizedMap(new WeakHashMap<String, HFCAClient>(2));
    private final HFClient fabricClient = HFClient.createNewInstance();
    @Value("${fabric.orderer.name}") private String defaultOrdererName;
    @Value("${fabric.orderer.endpoint}") String defaultOrdererEndpoint;
    private final ThreadLocal<User> USER_CONTEXT = new ThreadLocal<>();
    @PostConstruct
    private void init() {
        try {
            fabricClient.setCryptoSuite(getCryptoSuite());
        } catch (InvalidArgumentException | CryptoException e) {
            LOG.error("failed to enable encryption for fabric client", e);
        }
    }
    public FabricSDK withUserContext(User userContext) {
        USER_CONTEXT.set(userContext);
        return this;
    }

    public Optional<Peer> withPeer(String peerId, String peerUrl) {
        Peer peer = null;
        try {
            fabricClient.setUserContext(USER_CONTEXT.get());
            peer = fabricClient.newPeer(peerId, peerUrl);
            PEER_CACHE.putIfAbsent(peerId, peer);
        } catch (Exception e) {
            LOG.error("failed to create peer {} on {}: ", peerId, peerUrl, e);
        }
        return Optional.ofNullable(peer);
    }

    public Optional<Orderer> withOrderer(String ordererName, String ordererUrl) {
        Orderer orderer = null;
        try {
            orderer = fabricClient.newOrderer(ordererName, ordererUrl);
        } catch (Exception e) {
            LOG.error("failed to create orderer {} on {}", ordererName, ordererUrl, e);
        }
        return Optional.ofNullable(orderer);
    }

    public Optional<EventHub> withEventHub(String eventhubId, String eventEndpoint) {
        EventHub eventHub = null;
        try {
            eventHub = fabricClient.newEventHub(eventhubId, eventEndpoint);
            EVENTHUB_CACHE.putIfAbsent(eventhubId, eventHub);
        } catch (Exception e) {
            LOG.error("failed to create event hub at {}", eventEndpoint, e);
        }
        return Optional.ofNullable(eventHub);
    }
    /**
     * @param chainName chain name
     * @param orderer with which orderer the chain will be constructed
     * @param channelConfiguration chain configuration
     */
    public Optional<Channel> constructChain(String chainName, Orderer orderer, ChannelConfiguration channelConfiguration) {
        Channel chain = null;
        try {
            Channel availableChain = fabricClient.getChannel(chainName);
            if (availableChain != null) {
                return Optional.of(availableChain);
            }

            Optional<ChainInfo> chainInfoOptional = chainRepo.findByNameAndOrderer(chainName, orderer.getUrl());
            fabricClient.setUserContext(USER_CONTEXT.get());
            chain = chainInfoOptional
              .map(existingChainInfo -> {
                  try {
                      Channel existingChain = fabricClient.newChannel(existingChainInfo.getName());
                      existingChain.addOrderer(orderer);

                      for (PeerEventhub peerEventhub : existingChainInfo.getPeers()) {
                          Peer peer = fabricClient.newPeer(peerEventhub.getId(), peerEventhub.getEndpoint());
                          PEER_CACHE.putIfAbsent(peer.getName(), peer);
                          existingChain.addPeer(peer);
                          existingChain.addEventHub(fabricClient.newEventHub(peerEventhub.getId(), peerEventhub.getEventhub()));
                      }
                      if (!existingChainInfo
                        .getPeers()
                        .isEmpty()) existingChain.initialize();
                      LOG.info("chain {} exist on orderer {}, rebuilding from data source", chainName, orderer.getUrl());
                      return existingChain;
                  } catch (InvalidArgumentException | TransactionException configuredChainException) {
                      LOG.error("failed to construct a configured chain {}", chainName, configuredChainException);
                  }
                  return null;
              })
              .orElseGet(() -> {
                  try {
                      Channel newChain = fabricClient.newChannel(chainName, orderer, channelConfiguration, fabricClient.getChannelConfigurationSignature(channelConfiguration, fabricClient.getUserContext()));
                      ChainInfo newChainInfo = new ChainInfo(chainName, orderer.getUrl());
                      newChainInfo.setAffiliation(fabricClient
                        .getUserContext()
                        .getAffiliation());
                      chainRepo.save(newChainInfo);
                      LOG.info("new chain {} constructed on orderer {}", chainName, orderer.getUrl());
                      return newChain;
                  } catch (TransactionException | InvalidArgumentException e) {
                      RestResp.set("failed to get current chain info "+chainName+" :"+e.getMessage());
                      LOG.warn("failed to construct new chain {} with orderer {} and configuration {}", chainName, orderer.getName(), e.getMessage());
                  }
                  return null;
              });
        } catch (Exception e) {
            LOG.error("failed to get current chain info of {} for orderer {}@{}", chainName, orderer.getName(), orderer.getUrl(), e);
        }
        return Optional.ofNullable(chain);
    }

    public Optional<Channel> getChain(String chainName) {
        final Channel cachedChain = fabricClient.getChannel(chainName);
        if (cachedChain == null) {
            return constructChain(chainName, null);
        } else {
            return Optional.of(cachedChain);
        }
    }

    public Optional<BlockchainInfo> getChaininfo(String chainName) {
        try {
            Optional<Channel> chainOptional = getChain(chainName);
            if (chainOptional.isPresent()) {
                return Optional.of(chainOptional
                  .get()
                  .queryBlockchainInfo());
            }
        } catch (Exception e) {
            LOG.error("failed to get default chain's block info", e);
        }
        return empty();
    }

    public Optional<Peer> getPeer(String peerId, String chainName) {
        return getChain(chainName)
          .map(chain -> chain
            .getPeers()
            .stream()
            .filter(peer -> peer
              .getName()
              .equals(peerId))
            .findFirst())
          .orElse(empty());
    }

    public Optional<Peer> getPeer(String peerId) {
        return Optional.ofNullable(PEER_CACHE.get(peerId));
    }

    public boolean joinChain(PeerEventhub peerEventhub, String chainName) {
        return withPeer(peerEventhub.getId(), peerEventhub.getEndpoint())
          .map(peer -> {
              boolean joined = joinChain(peer, chainName) && withEventHub(peerEventhub.getId(), peerEventhub.getEventhub())
                .map(eventHub -> attachEventHubToChain(chainName, eventHub))
                .orElse(false);
              LOG.info("peer {} joined chain {}, updating db...", peerEventhub.getId(), chainName);
              chainRepo
                .findByNameAndOrderer(chainName, defaultOrdererEndpoint)
                .ifPresent(chainInfo -> {
                    chainRepo.save(chainInfo.addPeer(peerEventhub));
                    LOG.info("peer {} joined chain {}, db updated!", peerEventhub.getId(), chainName);
                });
              return joined;
          })
          .orElse(false);
    }

    public boolean joinChain(Peer peer, String chainName) {
        Channel chain = fabricClient.getChannel(chainName);
        LOG.info("{} joining in chain {}", peer.getName(), chainName);
        try {
            boolean joined = chain
              .getPeers()
              .stream()
              .anyMatch(p -> peer
                .getName()
                .equals(p.getName()));
            if (!joined) {
                chain
                  .joinPeer(peer)
                  .initialize();
            }
            return true;
        } catch (Exception e) {
            RestResp.set(peer.getName()+" failed to join chain :"+chainName+" : Cause By this peer already join to channel"+chainName,true);
            LOG.error("{} failed to join chain {}: ", peer.getName(), chainName, e);
        }
        return false;
    }

    public List<Peer> chainPeers(String channelName) {
        LOG.debug("current peers of chain {}", channelName);
        return getChain(channelName)
          .map(chain -> newArrayList(chain.getPeers()))
          .orElse(newArrayList());
    }

    public List<EventHub> chainEventHubs(String channelName) {
        return Lists.newCopyOnWriteArrayList(fabricClient
          .getChannel(channelName)
          .getEventHubs());
    }

    public List<ProposalResponse> installChaincodeOnPeer(ChaincodeID chaincodeId, Channel channel, String lang, String sourceLocation, Collection<Peer> peers) {
        switch (lang) {
        case "go":
            return installChaincodeOnPeer(chaincodeId, channel, GO_LANG, sourceLocation, peers);
        case "java":
            return installChaincodeOnPeer(chaincodeId, channel, JAVA, sourceLocation, peers);
        default:
            break;
        }
        return emptyList();
    }

    public List<ProposalResponse> installChaincodeOnPeer(ChaincodeID chaincodeId, Channel channel, TransactionRequest.Type type, String sourceLocation, Collection<Peer> peers) {
        InstallProposalRequest installProposalRequest = fabricClient.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeId);
        try {
            installProposalRequest.setChaincodeSourceLocation(new File(sourceLocation));
            installProposalRequest.setChaincodeLanguage(type);
            Collection<ProposalResponse> responses = fabricClient.sendInstallProposal(installProposalRequest, peers);
            if (responses.isEmpty()) {
                LOG.warn("no response while installing chaincode {}", chaincodeId.getName());
            } else {
                CHAINCODE_CACHE.putIfAbsent(chaincodeId.getName(), chaincodeId);
                return newArrayList(responses);
            }
        } catch (Exception e) {
            LOG.error("failed to install chaincode {} on for chain {}", chaincodeId.getName(), channel.getName(), e);
        }
        return emptyList();
    }

    public CompletableFuture<ProposalResponse> instantiateChaincode(ChaincodeID chaincode, String chainname, ChaincodeEndorsementPolicy policy, String... params) {
        return getChain(chainname)
          .map(chain -> instantiateChaincode(chaincode, chain, chain
            .getPeers()
            .iterator()
            .next(), policy, params))
          .orElse(supplyAsync(() -> null));
    }

    public CompletableFuture<ProposalResponse> instantiateChaincode(ChaincodeID chaincode, Channel channel, Peer peer, ChaincodeEndorsementPolicy policy, final String... args) {
        InstantiateProposalRequest instantiateProposalRequest = fabricClient.newInstantiationProposalRequest();
        instantiateProposalRequest.setChaincodeID(chaincode);
        if (args.length > 0) instantiateProposalRequest.setFcn(args[0]);
        if (args.length > 1) instantiateProposalRequest.setArgs(Arrays.copyOfRange(args, 1, args.length));
        else instantiateProposalRequest.setArgs(EMPTY_ARGS);
        instantiateProposalRequest.setChaincodeEndorsementPolicy(policy);
        try {
            //TODO transient map not used
            instantiateProposalRequest.setTransientMap(emptyMap());
            Collection<ProposalResponse> responses = channel.sendInstantiationProposal(instantiateProposalRequest, singletonList(peer));
            if (responses.isEmpty()) {
                LOG.warn("no responses while instantiating chaincode {}", chaincode.getName());
            } else {
                final ProposalResponse response = responses
                  .iterator()
                  .next();
                if (response.getStatus() == SUCCESS) {
                    return channel
                      .sendTransaction(responses, channel.getOrderers())
                      .thenApply(transactionEvent -> {
                          LOG.info("instantiation {} : transaction {} finished", Arrays.toString(args), transactionEvent.getTransactionID());
                          return response;
                      });
                } else supplyAsync(() -> response);
            }
        } catch (Exception e) {
            LOG.error("failed to instantiate chaincode {} with arg {}", chaincode.getName(), Arrays.toString(args), e);
        }
        return supplyAsync(() -> null);
    }

    public CompletableFuture<ProposalResponse> upgradeChaincode(ChaincodeID chaincode, Channel channel, Peer peer, ChaincodeEndorsementPolicy policy, final String... args) {
        UpgradeProposalRequest upgradeProposalRequest = fabricClient.newUpgradeProposalRequest();
        if (args.length > 0) upgradeProposalRequest.setFcn(args[0]);
        if (args.length > 1) upgradeProposalRequest.setArgs(Arrays.copyOfRange(args, 1, args.length));
        else upgradeProposalRequest.setArgs(EMPTY_ARGS);
        upgradeProposalRequest.setChaincodeEndorsementPolicy(policy);
        upgradeProposalRequest.setChaincodeID(chaincode);
        upgradeProposalRequest.setChaincodeVersion(chaincode.getVersion());
        upgradeProposalRequest.setChaincodeName(chaincode.getName());
        try {
            //TODO transient map not used
            upgradeProposalRequest.setTransientMap(emptyMap());
            Collection<ProposalResponse> responses = channel.sendUpgradeProposal(upgradeProposalRequest, singletonList(peer));
            if (responses.isEmpty()) {
                LOG.warn("no responses while upgrade chaincode {}", chaincode.getName());
            } else {
                final ProposalResponse response = responses
                        .iterator()
                        .next();
                if (response.getStatus() == SUCCESS) {
                    return channel
                            .sendTransaction(responses, channel.getOrderers())
                            .thenApply(transactionEvent -> {
                                LOG.info("upgrade {} : transaction {} finished", Arrays.toString(args), transactionEvent.getTransactionID());
                                return response;
                            });
                } else supplyAsync(() -> response);
            }
        } catch (Exception e) {
            LOG.error("failed to upgrade chaincode {} with arg {}", chaincode.getName(), Arrays.toString(args), e);
        }
        return supplyAsync(() -> null);
    }

    public Optional<ChaincodeID> getChaincode(String chaincodeName) {
        return Optional.ofNullable(CHAINCODE_CACHE.get(chaincodeName));
    }

    public CompletableFuture<ProposalResponse> invokeChaincode(ChainCodeInfo chainCodeInfo, ChaincodeID chaincode, Channel channel, Peer peer, String... args) {
        TransactionProposalRequest transactionProposalRequest = fabricClient.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincode);
        /*if (args.length > 0) transactionProposalRequest.setFcn(args[0]);
        if (args.length > 1) transactionProposalRequest.setArgs(Arrays.copyOfRange(args, 1, args.length));
        else transactionProposalRequest.setArgs(EMPTY_ARGS);*/
        if (StringUtils.isNotBlank(chainCodeInfo.getFunction())){
            transactionProposalRequest.setFcn(chainCodeInfo.getFunction());
        } else {
            LOG.error("please input function: invoke or other");
        }

        if (args.length != 0){
            transactionProposalRequest.setArgs(args);
        } else {
            LOG.error("params is null");
        }

        try {
            Collection<ProposalResponse> responses = channel.sendTransactionProposal(transactionProposalRequest, singletonList(peer));
            if (responses.isEmpty()) {
                LOG.warn("no responses while invoking chaincode {}", chaincode.getName());
            } else {
                final ProposalResponse response = responses
                  .iterator()
                  .next();
                if (response.getStatus() == SUCCESS) {
                    return channel
                      .sendTransaction(responses)
                      .thenApply(transactionEvent -> {
                          LOG.info("invoking {} : transactionId {} finished", Arrays.toString(args), transactionEvent.getTransactionID());
                          return response;
                      });
                } else return supplyAsync(() -> response);
            }
        } catch (Exception e) {
            LOG.error("failed to invoke chaincode {} with arg {}", chaincode.getName(), Arrays.toString(args));
        }
        return supplyAsync(() -> null);
    }

    public CompletableFuture<ProposalResponse> upgradeChaincode(ChaincodeID chaincode, String chainname, ChaincodeEndorsementPolicy policy, String... params) {
        return getChain(chainname)
                .map(chain -> upgradeChaincode(chaincode, chain, chain
                        .getPeers()
                        .iterator()
                        .next(), policy, params))
                .orElse(supplyAsync(() -> null));
    }

    public CompletableFuture<ProposalResponse> invokeChaincode(ChainCodeInfo chainCodeInfo, String chainname, ChaincodeID chaincode, String... params) {
        return getChain(chainname)
          .map(chain -> invokeChaincode(chainCodeInfo, chaincode, chain, chain
            .getPeers()
            .iterator()
            .next(), params))
          .orElse(supplyAsync(() -> null));
    }

    public ProposalResponse queryChaincode(ChainCodeInfo chainCodeInfo, String chainname, ChaincodeID chaincode, String... args) {
        return getChain(chainname).map(chain -> queryChaincode(chainCodeInfo, chaincode, chain, chain.getPeers().iterator().next(), args)).orElse(null);
    }

    public ProposalResponse queryChaincode(ChainCodeInfo chainCodeInfo, ChaincodeID chaincode, Channel channel, Peer peer, String... args) {
        QueryByChaincodeRequest queryByChaincodeRequest = fabricClient.newQueryProposalRequest();
        queryByChaincodeRequest.setChaincodeID(chaincode);
        /*if (args.length > 0) queryByChaincodeRequest.setFcn(args[0]);
        if (args.length > 1) queryByChaincodeRequest.setArgs(Arrays.copyOfRange(args, 1, args.length));
        else queryByChaincodeRequest.setArgs(EMPTY_ARGS);*/
        if (StringUtils.isNotBlank(chainCodeInfo.getFunction())) {
            queryByChaincodeRequest.setFcn(chainCodeInfo.getFunction());
        } else {
            LOG.error("please input function: query or other");
        }

        if (args.length != 0) {
            queryByChaincodeRequest.setArgs(args);
        } else {
            LOG.error("params is null");
        }

        try {
            Collection<ProposalResponse> responses = channel.queryByChaincode(queryByChaincodeRequest, singletonList(peer));
            if (responses.isEmpty()) {
                LOG.warn("no response while querying chaincode {}", chaincode.getName());
            } else {
                ProposalResponse response = responses
                  .iterator()
                  .next();
                if (response.getStatus() == SUCCESS) return response;
            }
        } catch (Exception e) {
            RestResp.set("failed to query chaincode "+chaincode.getName()+" with "+Arrays.toString(args)+" Cause by agrs is no exist or Method unsupported");
            LOG.error("failed to query chaincode {} with arg {}", chaincode.getName(), Arrays.toString(args), e);
        }
        return null;
    }

    public Set<String> chainsOfPeer(Peer peer) {
        try {
            return fabricClient.queryChannels(peer);
        } catch (Exception e) {
            LOG.error("failed to query chains of peer {}:", peer.getName(), e);
        }
        return emptySet();
    }

    public List<Query.ChaincodeInfo> chaincodesOnPeer(Peer peer) {
        try {
            fabricClient.setUserContext(USER_CONTEXT.get());
            return fabricClient.queryInstalledChaincodes(peer);
        } catch (Exception e) {
            LOG.error("failed to query installed chaincodes of peer {}:", peer.getName(), e);
        }
        return emptyList();
    }

    /**
     * instantiated chaincodes
     */
    public List<Query.ChaincodeInfo> chaincodesOnPeer(Peer peer, Channel channel) {
        try {
            return channel.queryInstantiatedChaincodes(peer);
        } catch (Exception e) {
            LOG.error("failed to query instantiated chaincodes of peer {}:", peer.getName(), e);
        }
        return emptyList();
    }

    public boolean revokeUser(String username, int reason, String caname, String cauri) {
        try {
            HFCAClient hfcaClient = getCaClient(caname, cauri);
            hfcaClient.revoke(USER_CONTEXT.get(), username, int2RevokeReason(reason).toString());
            return true;
        } catch (Exception e) {
            LOG.error("failed to revoke {} with reason {}", username, reason, e);
        }
        return false;
    }

    private RevokeReason int2RevokeReason(int reasonOrdinal) {
        Optional<RevokeReason> revokeReasonOptional = Arrays
          .stream(RevokeReason.values())
          .filter(r -> r.ordinal() == reasonOrdinal)
          .findFirst();
        return revokeReasonOptional.orElse(UNSPECIFIED);
    }

    public boolean register(String username, String password, String ca, String caUri) {
        try {
            User context = USER_CONTEXT.get();
            RegistrationRequest registrationRequest = new RegistrationRequest(username, context.getAffiliation());
            //registrationRequest.setCAName(ca);
            registrationRequest.setType("user");
            registrationRequest.setSecret(password);
            HFCAClient hfcaClient = getCaClient(ca, caUri);
            String registerResult = hfcaClient.register(registrationRequest, context);
            System.out.println(registerResult+"------------------------");
            LOG.info("fabric registered {} of {}: {} by {}", username, context.getAffiliation(), registerResult, context.getName());
            return true;
        } catch (Exception e) {
            String message = e.getMessage().split("Identity")[1].split("\"}],\"message")[0];
            RestResp.set(message);
            LOG.error("failed to register new fabric user on {}@{}: {}", ca, caUri, e.getMessage());
        }
        return false;
    }

    private HFCAClient getCaClient(final String caname, final String cauri) {
        return CA_CLIENTS.computeIfAbsent(caname, key -> {
            try {
                HFCAClient client = HFCAClient.createNewInstance(caname, cauri, new Properties());
                client.setCryptoSuite(getCryptoSuite());
                return client;
            } catch (Exception e) {
                LOG.error("failed to create ca client of {}@{}: {}", caname, cauri, e.getMessage());
                throw new IllegalArgumentException("cannot create ca client instance for " + caname + "@" + cauri);
            }
        });
    }

    public Optional<Enrollment> enroll(String username, String password, String caname, String uri) {
        try {
            HFCAClient hfcaClient = getCaClient(caname, uri);
            Enrollment enrollment = hfcaClient.enroll(username, password);
            LOG.info("user {} enrolled", username);
            return Optional.of(enrollment);
        } catch (Exception e) {
            LOG.error("failed to enroll user {}: {}", username, e.getMessage());
        }
        return empty();
    }

    public boolean attachEventHubToChain(String chainname, EventHub eventHub) {
        try {
            Channel channel = fabricClient.getChannel(chainname);
            channel.addEventHub(eventHub);
            channel.initialize();
            LOG.info("chain {} listening on eventhub {}", chainname, chainname, eventHub.getName());
            return true;
        } catch (Exception e) {
            LOG.error("failed to attach eventhub {}", eventHub.getUrl(), e.getMessage());
        }
        return false;
    }

    public List<BlockInfo> getChainBlocks(String chainname) {
        try {
            Optional<Channel> chainOptional = getChain(chainname);
            if (chainOptional.isPresent()) {
                Channel channel = chainOptional.get();
                long chainheight = channel
                  .queryBlockchainInfo()
                  .getHeight();
                return LongStream
                  .range(Math.max(0, chainheight - 5), chainheight)
                  .parallel()
                  .mapToObj(i -> {
                      try {
                          return channel.queryBlockByNumber(i);
                      } catch (Exception e) {
                          RestResp.set("failed to get "+i+"-th block :"+e.getMessage());
                          LOG.error("failed to get {}-th block", i, e);
                      }
                      return null;
                  })
                  .filter(Objects::nonNull)
                  .collect(toList());
            }
        } catch (Exception e) {
            RestResp.set("failed to get default chain's block info :"+e.getMessage());
            LOG.error("failed to get default chain's block info", e);
        }
        return emptyList();
    }

    public Optional<BlockInfo> getChainBlock(String chainname, long blockNumber) {
        Optional<Channel> chainOptional = getChain(chainname);
        if (chainOptional.isPresent()) {
            Channel chain = chainOptional.get();
            try {
                return Optional.of(chain.queryBlockByNumber(blockNumber));
            } catch (Exception e) {
                LOG.error("failed to get {}-th block  of default chain", blockNumber, e);
            }
        }
        return empty();
    }

    public Optional<BlockInfo> getChainBlock(String chainname, String tx) {
        Optional<Channel> chainOptional = getChain(chainname);
        if (chainOptional.isPresent()) {
            Channel chain = chainOptional.get();
            try {
                return Optional.of(chain.queryBlockByTransactionID(tx));
            } catch (Exception e) {
                LOG.error("failed to get block of default chain by tx {}", tx, e);
            }
        }
        return empty();
    }

    public Optional<TransactionInfo> getChainTx(String chainname, String tx) {
        Optional<Channel> chainOptional = getChain(chainname);
        if (chainOptional.isPresent()) {
            Channel chain = chainOptional.get();
            try {
                return Optional.of(chain.queryTransactionByID(tx));
            } catch (Exception e) {
                LOG.error("failed to get tx {} of default chain", tx, e);
            }
        }
        return empty();
    }

    public void stopEventhub(String peerId) {
        EVENTHUB_CACHE.computeIfPresent(peerId, (key, eventHub) -> {
            eventHub.shutdown();
            return null;
        });
    }

    public List<ProposalResponse> installChaincodeOnPeer(ChaincodeID chaincode, String chainname, String path, String lang, List<Peer> peers) {
        switch (lang) {
        case "go":
            return getChain(chainname)
              .map(chain -> installChaincodeOnPeer(chaincode, chain, GO_LANG, path, peers))
              .orElse(emptyList());
        case "java":
            return getChain(chainname)
              .map(chain -> installChaincodeOnPeer(chaincode, chain, JAVA, path, peers))
              .orElse(emptyList());
        default:
            break;
        }
        return emptyList();
    }

    public Optional<Channel> constructChain(String chain, ChannelConfiguration chainConfiguration) {
        Optional<Orderer> ordererOptional = withOrderer(defaultOrdererName, defaultOrdererEndpoint);
        return ordererOptional.flatMap(orderer -> constructChain(chain, orderer, chainConfiguration));
    }

    public List<ChainInfo> chains() {
        return newArrayList(chainRepo.findByAffiliation(USER_CONTEXT
          .get()
          .getAffiliation()))
          .stream()
          .map(chainInfo -> getChaininfo(chainInfo.getName())
            .map(chainInfo::withBlockchainInfo)
            .orElse(chainInfo))
          .collect(toList());
    }
}
