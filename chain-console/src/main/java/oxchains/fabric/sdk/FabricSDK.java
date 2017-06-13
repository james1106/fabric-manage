package oxchains.fabric.sdk;

import com.google.common.collect.Lists;
import org.hyperledger.fabric.protos.peer.Query.ChaincodeInfo;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.RevokeReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import oxchains.fabric.console.data.ChainRepo;
import oxchains.fabric.console.domain.ChainInfo;
import oxchains.fabric.console.domain.PeerEventhub;

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
import static org.hyperledger.fabric.sdk.ChainCodeResponse.Status.SUCCESS;
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
    private final WeakHashMap<String, ChainCodeID> CHAINCODE_CACHE = new WeakHashMap<>(8);
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
     * @param chainConfiguration chain configuration
     */
    public Optional<Chain> constructChain(String chainName, Orderer orderer, ChainConfiguration chainConfiguration) {
        Chain chain = null;
        try {
            Optional<ChainInfo> chainInfoOptional = chainRepo.findByNameAndOrderer(chainName, orderer.getUrl());
            fabricClient.setUserContext(USER_CONTEXT.get());
            chain = chainInfoOptional
              .map(existingChainInfo -> {
                  try {
                      Chain existingChain = fabricClient.newChain(existingChainInfo.getName());
                      existingChain.addOrderer(orderer);

                      for (PeerEventhub peerEventhub : existingChainInfo.getPeers()) {
                          Peer peer = fabricClient.newPeer(peerEventhub.getId(), peerEventhub.getEndpoint());
                          PEER_CACHE.putIfAbsent(peer.getName(), peer);
                          existingChain.addPeer(peer);
                          existingChain.addEventHub(fabricClient.newEventHub(peerEventhub.getId(), peerEventhub.getEventhub()));
                      }
                      existingChain.initialize();
                      LOG.info("chain {} exist on orderer {}, rebuilding from data source", chainName, orderer.getUrl());
                      return existingChain;
                  } catch (InvalidArgumentException | TransactionException configuredChainException) {
                      LOG.error("failed to construct a configured chain {}", chainName, configuredChainException);
                  }
                  return null;
              })
              .orElseGet(() -> {
                  try {
                      Chain newChain = fabricClient.newChain(chainName, orderer, chainConfiguration, fabricClient.getChainConfigurationSignature(chainConfiguration, fabricClient.getUserContext()));
                      ChainInfo newChainInfo = new ChainInfo(chainName, orderer.getUrl());
                      newChainInfo.setAffiliation(fabricClient.getUserContext().getAffiliation());
                      chainRepo.save(newChainInfo);
                      LOG.info("new chain {} constructed on orderer {}", chainName, orderer.getUrl());
                      return newChain;
                  } catch (TransactionException | InvalidArgumentException e) {
                      LOG.warn("failed to construct new chain {} with orderer {} and configuration {}", chainName, orderer.getName(), e.getMessage());
                  }
                  return null;
              });

        } catch (Exception e) {
            LOG.error("failed to get current chain info of {} for orderer {}@{}", chainName, orderer.getName(), orderer.getUrl(), e);
        }
        return Optional.ofNullable(chain);
    }

    public Optional<Chain> getChain(String chainName) {
        final Chain cachedChain = fabricClient.getChain(chainName);
        return Optional.ofNullable(cachedChain);
    }

    public Optional<BlockchainInfo> getChaininfo(String chainName) {
        try {
            Optional<Chain> chainOptional = getChain(chainName);
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

    public boolean joinChain(Peer peer, String chainName) {
        Chain chain = fabricClient.getChain(chainName);
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
            LOG.error("{} failed to join chain {}: ", peer.getName(), chainName, e);
        }
        return false;
    }

    public List<Peer> chainPeers(String channelName) {
        return Lists.newCopyOnWriteArrayList(fabricClient
          .getChain(channelName)
          .getPeers());
    }

    public List<EventHub> chainEventHubs(String channelName) {
        return Lists.newCopyOnWriteArrayList(fabricClient
          .getChain(channelName)
          .getEventHubs());
    }

    public List<ProposalResponse> installChaincodeOnPeer(ChainCodeID chaincodeId, Chain chain, String lang, String sourceLocation, Collection<Peer> peers) {
        switch (lang) {
        case "go":
            return installChaincodeOnPeer(chaincodeId, chain, GO_LANG, sourceLocation, peers);
        case "java":
            return installChaincodeOnPeer(chaincodeId, chain, JAVA, sourceLocation, peers);
        default:
            break;
        }
        return emptyList();
    }

    public List<ProposalResponse> installChaincodeOnPeer(ChainCodeID chaincodeId, Chain chain, TransactionRequest.Type type, String sourceLocation, Collection<Peer> peers) {
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
            LOG.error("failed to install chaincode {} on for chain {}", chaincodeId.getName(), chain.getName(), e);
        }
        return emptyList();
    }

    public CompletableFuture<ProposalResponse> instantiateChaincode(ChainCodeID chaincode, String chainname, ChaincodeEndorsementPolicy policy, String... params) {
        return getChain(chainname)
          .map(chain -> instantiateChaincode(chaincode, chain, chain
            .getPeers()
            .iterator()
            .next(), policy, params))
          .orElse(supplyAsync(() -> null));
    }

    public CompletableFuture<ProposalResponse> instantiateChaincode(ChainCodeID chaincode, Chain chain, Peer peer, ChaincodeEndorsementPolicy policy, final String... args) {
        InstantiateProposalRequest instantiateProposalRequest = fabricClient.newInstantiationProposalRequest();
        instantiateProposalRequest.setChaincodeID(chaincode);
        if (args.length > 0) instantiateProposalRequest.setFcn(args[0]);
        if (args.length > 1) instantiateProposalRequest.setArgs(Arrays.copyOfRange(args, 1, args.length));
        else instantiateProposalRequest.setArgs(EMPTY_ARGS);
        instantiateProposalRequest.setChaincodeEndorsementPolicy(policy);
        try {
            //TODO transient map not used
            instantiateProposalRequest.setTransientMap(emptyMap());
            Collection<ProposalResponse> responses = chain.sendInstantiationProposal(instantiateProposalRequest, singletonList(peer));
            if (responses.isEmpty()) {
                LOG.warn("no responses while instantiating chaincode {}", chaincode.getName());
            } else {
                final ProposalResponse response = responses
                  .iterator()
                  .next();
                if (response.getStatus() == SUCCESS) {
                    return chain
                      .sendTransaction(responses, chain.getOrderers())
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

    public Optional<ChainCodeID> getChaincode(String chaincodeName) {
        return Optional.ofNullable(CHAINCODE_CACHE.get(chaincodeName));
    }

    public CompletableFuture<ProposalResponse> invokeChaincode(ChainCodeID chaincode, Chain chain, Peer peer, String... args) {
        TransactionProposalRequest transactionProposalRequest = fabricClient.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincode);
        if (args.length > 0) transactionProposalRequest.setFcn(args[0]);
        if (args.length > 1) transactionProposalRequest.setArgs(Arrays.copyOfRange(args, 1, args.length));
        else transactionProposalRequest.setArgs(EMPTY_ARGS);
        try {
            Collection<ProposalResponse> responses = chain.sendTransactionProposal(transactionProposalRequest, singletonList(peer));
            if (responses.isEmpty()) {
                LOG.warn("no responses while invoking chaincode {}", chaincode.getName());
            } else {
                final ProposalResponse response = responses
                  .iterator()
                  .next();
                if (response.getStatus() == SUCCESS) {
                    return chain
                      .sendTransaction(responses)
                      .thenApply(transactionEvent -> {
                          LOG.info("invoking {} : transaction {} finished", Arrays.toString(args), transactionEvent.getTransactionID());
                          return response;
                      });
                } else return supplyAsync(() -> response);
            }
        } catch (Exception e) {
            LOG.error("failed to invoke chaincode {} with arg {}", chaincode.getName(), Arrays.toString(args));
        }
        return supplyAsync(() -> null);
    }

    public CompletableFuture<ProposalResponse> invokeChaincode(String chainname, ChainCodeID chaincode, String... params) {
        return getChain(chainname)
          .map(chain -> invokeChaincode(chaincode, chain, chain
            .getPeers()
            .iterator()
            .next(), params))
          .orElse(supplyAsync(() -> null));
    }

    public ProposalResponse queryChaincode(String chainname, ChainCodeID chaincode, String... args) {
        return getChain(chainname)
          .map(chain -> queryChaincode(chaincode, chain, chain
            .getPeers()
            .iterator()
            .next(), args))
          .orElse(null);
    }

    public ProposalResponse queryChaincode(ChainCodeID chaincode, Chain chain, Peer peer, String... args) {
        QueryByChaincodeRequest queryByChaincodeRequest = fabricClient.newQueryProposalRequest();
        queryByChaincodeRequest.setChaincodeID(chaincode);
        if (args.length > 0) queryByChaincodeRequest.setFcn(args[0]);
        if (args.length > 1) queryByChaincodeRequest.setArgs(Arrays.copyOfRange(args, 1, args.length));
        else queryByChaincodeRequest.setArgs(EMPTY_ARGS);

        try {
            Collection<ProposalResponse> responses = chain.queryByChaincode(queryByChaincodeRequest, singletonList(peer));
            if (responses.isEmpty()) {
                LOG.warn("no response while querying chaincode {}", chaincode.getName());
            } else {
                ProposalResponse response = responses
                  .iterator()
                  .next();
                if (response.getStatus() == SUCCESS) return response;
            }
        } catch (Exception e) {
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

    public List<ChaincodeInfo> chaincodesOnPeer(Peer peer) {
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
    public List<ChaincodeInfo> chaincodesOnPeer(Peer peer, Chain chain) {
        try {
            return chain.queryInstantiatedChaincodes(peer);
        } catch (Exception e) {
            LOG.error("failed to query instantiated chaincodes of peer {}:", peer.getName(), e);
        }
        return emptyList();
    }

    public boolean revokeUser(String username, int reason, String caname, String cauri) {
        try {
            HFCAClient hfcaClient = getCaClient(caname, cauri);
            hfcaClient.revoke(USER_CONTEXT.get(), username, int2RevokeReason(reason));
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
            registrationRequest.setCAName(ca);
            registrationRequest.setType("user");
            registrationRequest.setSecret(password);
            HFCAClient hfcaClient = getCaClient(ca, caUri);
            String registerResult = hfcaClient.register(registrationRequest, context);
            LOG.info("fabric registered {} of {}: {} by {}", username, context.getAffiliation(), registerResult, context.getName());
            return true;
        } catch (Exception e) {
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
            Chain chain = fabricClient.getChain(chainname);
            chain.addEventHub(eventHub);
            chain.initialize();
            return true;
        } catch (Exception e) {
            LOG.error("failed to attach eventhub {}", eventHub.getUrl(), e.getMessage());
        }
        return false;
    }

    public List<BlockInfo> getChainBlocks(String chainname) {
        try {
            Optional<Chain> chainOptional = getChain(chainname);
            if (chainOptional.isPresent()) {
                Chain chain = chainOptional.get();
                long chainheight = chain
                  .queryBlockchainInfo()
                  .getHeight();
                return LongStream
                  .range(Math.max(0, chainheight - 5), chainheight)
                  .parallel()
                  .mapToObj(i -> {
                      try {
                          return chain.queryBlockByNumber(i);
                      } catch (Exception e) {
                          LOG.error("failed to get {}-th block", i, e);
                      }
                      return null;
                  })
                  .filter(Objects::nonNull)
                  .collect(toList());
            }
        } catch (Exception e) {
            LOG.error("failed to get default chain's block info", e);
        }
        return emptyList();
    }

    public Optional<BlockInfo> getChainBlock(String chainname, long blockNumber) {
        Optional<Chain> chainOptional = getChain(chainname);
        if (chainOptional.isPresent()) {
            Chain chain = chainOptional.get();
            try {
                return Optional.of(chain.queryBlockByNumber(blockNumber));
            } catch (Exception e) {
                LOG.error("failed to get {}-th block  of default chain", blockNumber, e);
            }
        }
        return empty();
    }

    public Optional<BlockInfo> getChainBlock(String chainname, String tx) {
        Optional<Chain> chainOptional = getChain(chainname);
        if (chainOptional.isPresent()) {
            Chain chain = chainOptional.get();
            try {
                return Optional.of(chain.queryBlockByTransactionID(tx));
            } catch (Exception e) {
                LOG.error("failed to get block of default chain by tx {}", tx, e);
            }
        }
        return empty();
    }

    public Optional<TransactionInfo> getChainTx(String chainname, String tx) {
        Optional<Chain> chainOptional = getChain(chainname);
        if (chainOptional.isPresent()) {
            Chain chain = chainOptional.get();
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

    public List<ProposalResponse> installChaincodeOnPeer(ChainCodeID chaincode, String chainname, String path, String lang, List<Peer> peers) {
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

    public Optional<Chain> constructChain(String chain, ChainConfiguration chainConfiguration) {
        Optional<Orderer> ordererOptional = withOrderer(defaultOrdererName, defaultOrdererEndpoint);
        return ordererOptional.flatMap(orderer -> constructChain(chain, orderer, chainConfiguration));
    }

    public List<ChainInfo> chains() {
        return newArrayList(chainRepo.findByAffiliation(USER_CONTEXT.get().getAffiliation()));
    }
}
