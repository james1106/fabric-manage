package oxchains.fabric.sdk;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.protos.peer.Query.ChaincodeInfo;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.RevokeReason;
import org.hyperledger.fabric_ca.sdk.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import oxchains.fabric.sdk.domain.CAAdmin;
import oxchains.fabric.sdk.domain.FabricUser;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

    private final String caServerUrl;
    private final Properties properties = new Properties();
    private final String[] EMPTY_ARGS = new String[] {};

    public FabricSDK(@Value("${fabric.ca.server.url}") String caServerUrl, Properties properties) {
        this.caServerUrl = caServerUrl;
        this.properties.putAll(properties);
    }

    private static final WeakHashMap<String, Peer> PEER_CACHE = new WeakHashMap<>(8);
    private static final WeakHashMap<String, EventHub> EVENTHUB_CACHE = new WeakHashMap<>(8);
    private static final WeakHashMap<String, ChainCodeID> CHAINCODE_CACHE = new WeakHashMap<>(8);

    private final HFClient fabricClient = HFClient.createNewInstance();
    private HFCAClient caClient;
    private CAAdmin caServerAdminUser;

    @Value("${fabric.ca.server.admin}") private String caServerAdmin;
    @Value("${fabric.ca.server.admin.pass}") private String caServerAdminPass;
    @Value("${fabric.ca.server.admin.mspid}") private String caServerAdminMSPId;
    @Value("${fabric.ca.server.admin.affiliation}") private String caServerAdminAffiliation;
    @Value("${fabric.orderer.name}") private String defaultOrdererName;
    @Value("${fabric.orderer.endpoint}") String defaultOrdererEndpoint;
    @Value("${fabric.chain.name}") String defaultChainName;
    @Value("${fabric.chain.configuration}") String defaultChainConfigurationPath;

    @PostConstruct
    private void init() {
        try {
            caClient = new HFCAClient(caServerUrl, properties);
            caClient.setCryptoSuite(getCryptoSuite());
            fabricClient.setCryptoSuite(getCryptoSuite());
            this.caServerAdminUser = new CAAdmin(caServerAdmin, caServerAdminAffiliation, caServerAdminMSPId);
            enroll(caServerAdminUser);
            fabricClient.setUserContext(caServerAdminUser);
            withOrderer(defaultOrdererName, defaultOrdererEndpoint).ifPresent(orderer -> {
                constructChain(defaultChainName, orderer, defaultChainConfigurationPath);
            });
        } catch (MalformedURLException e) {
            LOG.error("failed to create CA client with url {} and properties {}", caServerUrl, properties, e);
        } catch (InvalidArgumentException | CryptoException e) {
            LOG.error("failed to enable encryption for fabric client", e);
        } catch (BaseException e) {
            LOG.error("failed to enroll admin user: ", e);
        }
    }

    /**
     * the user will be enrolled if hasn't
     */
    private void enroll(CAAdmin userToEnroll) throws BaseException {
        //TODO check if enrolled yet
        userToEnroll.setEnrollment(caClient.enroll(userToEnroll.getName(), caServerAdminPass));
    }

    public Optional<Peer> withPeer(String peerId, String peerUrl) {
        Peer peer = null;
        try {
            peer = fabricClient.newPeer(peerId, peerUrl);
            PEER_CACHE.putIfAbsent(peerId, peer);
        } catch (Exception e) {
            LOG.error("failed to create peer {} on {}: ", peerId, peerUrl, e);
        }
        return Optional.ofNullable(peer);
    }


    public Optional<FabricUser> createUser(String username, String affiliation) {
        FabricUser user = null;
        try {
            user = new FabricUser(username, affiliation);
            RegistrationRequest registrationRequest = new RegistrationRequest(username, affiliation);
            user.setPassword(caClient.register(registrationRequest, caServerAdminUser));
            //TODO when to enroll? difference to register?
            caClient.enroll(username, user.getPassword());
            user.setMspId(caServerAdminMSPId);
        } catch (Exception e) {
            LOG.error("failed to register fabric user {} from {}", username, affiliation);
        }
        return Optional.ofNullable(user);
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
     * @param chainConfigurationFilePath path of chain configuration file
     */
    public Optional<Chain> constructChain(String chainName, Orderer orderer, String chainConfigurationFilePath) {
        try {
            InputStream inputStream = getClass()
              .getClassLoader()
              .getResourceAsStream(chainConfigurationFilePath);
            if (inputStream != null) {
                return constructChain(chainName, orderer, new ChainConfiguration(IOUtils.toByteArray(inputStream)));
            }
        } catch (IOException e) {
            LOG.error("failed to read chain configuration file {}", chainConfigurationFilePath, e);
        }
        return empty();
    }

    /**
     * @param chainName chain name
     * @param orderer with which orderer the chain will be constructed
     * @param chainConfiguration chain configuration
     */
    public Optional<Chain> constructChain(String chainName, Orderer orderer, ChainConfiguration chainConfiguration) {
        Chain chain = null;
        try {
            chain = fabricClient.newChain(chainName, orderer, chainConfiguration);
        } catch (TransactionException | InvalidArgumentException e) {
            LOG.warn("failed to construct new chain {} with orderer {} and configuration {}", chainName, orderer.getName(), e.getMessage());
            try {
                chain = fabricClient.newChain(chainName);
                chain.addOrderer(orderer);
            } catch (InvalidArgumentException configuredChainExceptoin) {
                LOG.error("failed to construct a configured chain {}", chainName, configuredChainExceptoin);
            }
        }
        return Optional.ofNullable(chain);
    }

    public Optional<Chain> getChain(String chainName) {
        final Chain cachedChain = fabricClient.getChain(chainName);
        return Optional.ofNullable(cachedChain);
    }

    public Optional<BlockchainInfo> getChaininfo() {
        try {
            Optional<Chain> chainOptional = getChain(defaultChainName);
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

    public Optional<Peer> getPeer(String peerId) {
        return Optional.ofNullable(PEER_CACHE.get(peerId));
    }

    public boolean joinChain(Peer peer) {
        return joinChain(peer.getName(), defaultChainName);
    }

    public boolean joinChain(String peerId, String chainName) {
        Chain chain = fabricClient.getChain(chainName);
        try {
            boolean joined = chain
              .getPeers()
              .stream()
              .anyMatch(peer -> peerId.equals(peer.getName()));
            if (!joined) {
                chain.joinPeer(PEER_CACHE.get(peerId));
                chain.initialize();
            }
            return true;
        } catch (Exception e) {
            LOG.error("{} failed to join chain {}: ", peerId, chainName, e);
        }
        return false;
    }

    public List<Peer> chainPeers(String channelName) {
        return Lists.newCopyOnWriteArrayList(fabricClient
          .getChain(channelName)
          .getPeers());
    }

    public List<EventHub> chainEventHubs() {
        return chainEventHubs(defaultChainName);
    }

    public List<EventHub> chainEventHubs(String channelName) {
        return Lists.newCopyOnWriteArrayList(fabricClient
          .getChain(channelName)
          .getEventHubs());
    }

    public List<Peer> chainPeers() {
        return chainPeers(defaultChainName);
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
            Collection<ProposalResponse> responses = chain.sendInstallProposal(installProposalRequest, peers);
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

    public CompletableFuture<ProposalResponse> instantiateChaincode(ChainCodeID chaincode, ChaincodeEndorsementPolicy policy, String... params) {
        return getChain(defaultChainName)
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

    public CompletableFuture<ProposalResponse> invokeChaincode(ChainCodeID chaincode, String... params) {
        return getChain(defaultChainName)
          .map(chain -> invokeChaincode(chaincode, chain, chain
            .getPeers()
            .iterator()
            .next(), params))
          .orElse(supplyAsync(() -> null));
    }

    public ProposalResponse queryChaincode(ChainCodeID chaincode, String... args) {
        return getChain(defaultChainName)
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
            return fabricClient.queryInstalledChaincodes(peer);
        } catch (Exception e) {
            LOG.error("failed to query installed chaincodes of peer {}:", peer.getName(), e);
        }
        return emptyList();
    }

    /**
     * instantiated chaincodes
     */
    public List<ChaincodeInfo> chaincodesOnPeerForDefaultChain(Peer peer) {
        return getChain(defaultChainName).map(chain -> chaincodesOnPeer(peer, chain)) .orElse(emptyList());
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

    public boolean revokeUser(String username, int reason) {
        try {
            caClient.revoke(caServerAdminUser, username, int2RevokeReason(reason));
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

    public boolean register(String username, String affiliation, String password) {
        try {
            RegistrationRequest registrationRequest = new RegistrationRequest(username, affiliation);
            registrationRequest.setSecret(password);
            String registerResult = caClient.register(registrationRequest, caServerAdminUser);
            LOG.info("fabric registered {} of {}: {}", username, affiliation, registerResult);
            return true;
        } catch (Exception e) {
            LOG.error("failed to register new fabric user {} of {}", username, affiliation, e);
        }
        return false;
    }

    public Optional<Enrollment> enroll(String username, String password) {
        try {
            Enrollment enrollment = caClient.enroll(username, password);
            LOG.info("user {} enrolled", username);
            return Optional.of(enrollment);
        } catch (Exception e) {
            LOG.error("failed to enroll user {}", username, e);
        }
        return empty();
    }

    public boolean attachEventHubToChain(EventHub eventHub) {
        try {
            Chain chain = fabricClient.getChain(defaultChainName);
            chain.addEventHub(eventHub);
            return true;
        } catch (Exception e) {
            LOG.error("failed to attach eventhub {}", eventHub.getUrl(), e);
        }
        return false;
    }

    public List<BlockInfo> getChainBlocks() {
        try {
            Optional<Chain> chainOptional = getChain(defaultChainName);
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

    public Optional<BlockInfo> getChainBlock(long blockNumber) {
        Optional<Chain> chainOptional = getChain(defaultChainName);
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

    public Optional<BlockInfo> getChainBlock(String tx) {
        Optional<Chain> chainOptional = getChain(defaultChainName);
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

    public Optional<TransactionInfo> getChainTx(String tx) {
        Optional<Chain> chainOptional = getChain(defaultChainName);
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

    public List<ProposalResponse> installChaincodeOnPeer(ChainCodeID chaincode, String path, String lang, List<Peer> peers) {
        switch (lang) {
        case "go":
            return getChain(defaultChainName)
              .map(chain -> installChaincodeOnPeer(chaincode, chain, GO_LANG, path, peers))
              .orElse(emptyList());
        case "java":
            return getChain(defaultChainName)
              .map(chain -> installChaincodeOnPeer(chaincode, chain, JAVA, path, peers))
              .orElse(emptyList());
        default:
            break;
        }
        return emptyList();
    }

}
