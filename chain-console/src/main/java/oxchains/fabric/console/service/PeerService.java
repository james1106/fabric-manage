package oxchains.fabric.console.service;

import org.hyperledger.fabric.sdk.EventHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import oxchains.fabric.console.auth.JwtAuthentication;
import oxchains.fabric.console.data.PeerRepo;
import oxchains.fabric.console.domain.ChainCodeInfo;
import oxchains.fabric.console.domain.PeerEventhub;
import oxchains.fabric.console.domain.PeerInfo;
import oxchains.fabric.console.domain.User;
import oxchains.fabric.sdk.FabricSDK;
import oxchains.fabric.sdk.FabricSSH;
import oxchains.fabric.sdk.FabricSSH.SSHResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static oxchains.fabric.sdk.domain.CAUser.fromUser;

/**
 * @author aiet
 */
@Service
public class PeerService {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private FabricSDK fabricSDK;
    private FabricSSH fabricSSH;
    private PeerRepo peerRepo;

    @Value("${fabric.peer.connect.timeout}") private int peerConnectTimeout;

    public PeerService(@Autowired FabricSDK fabricSDK, @Autowired FabricSSH fabricSSH, @Autowired PeerRepo peerRepo) {
        this.fabricSDK = fabricSDK;
        this.fabricSSH = fabricSSH;
        this.peerRepo = peerRepo;
    }

    public List<PeerInfo> allPeers() {
        try {
            return userContext()
              .map(u -> newArrayList(peerRepo.findPeerEventhubsByAffiliation(u.getAffiliation()))
                .parallelStream()
                .map(peer -> fabricSDK
                  .withUserContext(fromUser(u))
                  .withPeer(peer.getId(), peer.getEndpoint())
                  .map(fabricPeer -> {
                      PeerInfo peerInfo = new PeerInfo(fabricPeer);
                      if (reachable(fabricPeer.getUrl())) {
                          peerInfo.setStatusCode(1);
                          peerInfo.setChaincodes(fabricSDK
                            .withUserContext(fromUser(u))
                            .chaincodesOnPeer(fabricPeer)
                            .stream()
                            .map(ChainCodeInfo::new)
                            .collect(toList()));
                          //FIXME find all chaincodes on the peer despite of the chain it is on
                          // peerInfo.setRunnablecodes(fabricSDK
                          //   .chaincodesOnPeerForDefaultChain(peer)
                          //   .stream()
                          //   .map(ChainCodeInfo::new)
                          //   .collect(toList()));
                          peerInfo.setChains(newArrayList(fabricSDK
                            .withUserContext(fromUser(u))
                            .chainsOfPeer(fabricPeer)));
                      }
                      return peerInfo;
                  })
                  .orElse(null))
                .collect(toList()))
              .orElse(emptyList());

        } catch (Exception e) {
            LOG.error("failed to fetch peers:", e);
        }
        return emptyList();
    }

    public boolean start(String peerId) {
        Optional<SSHResponse> responseOptional = fabricSSH.startPeer(peerId);
        if (responseOptional.isPresent()) return responseOptional
          .map(SSHResponse::succeeded)
          .orElse(false);
        return false;
    }

    public boolean stop(String peerId) {
        try {
            PeerEventhub peerEventhub = peerRepo.findOne(peerId);
            if (nonNull(peerEventhub.getEventhub())) fabricSDK.stopEventhub(peerId);
        } catch (Exception e) {
            LOG.error("failed to stop cached eventhub {}", peerId, e);
        }
        Optional<SSHResponse> responseOptional = fabricSSH.stopPeer(peerId);
        if (responseOptional.isPresent()) return responseOptional
          .map(SSHResponse::succeeded)
          .orElse(false);
        return false;
    }

    private Optional<User> userContext() {
        return ((JwtAuthentication) getContext().getAuthentication()).user();
    }

    public boolean addPeer(PeerEventhub peerEventhub) {
        try {
            if (!peerRepo
              .findPeerEventhubById(peerEventhub.getId())
              .isPresent()) {
                return userContext()
                  .map(context -> {
                      boolean registered = fabricSDK
                        .withUserContext(fromUser(context))
                        .register(peerEventhub.getId(), peerEventhub.getPassword(), context.getCa(), context.getUri());
                      if (registered) {
                          peerEventhub.inheritMSP(context);
                          return peerRepo.save(peerEventhub);
                      }
                      return null;
                  })
                  .isPresent();
            }
        } catch (Exception e) {
            LOG.error("failed to add peer and eventhub {}", peerEventhub, e);
        }
        return false;
    }

    private boolean eventHubAttached(String id, String endpoint) {
        if (nonNull(endpoint)) {
            Optional<EventHub> eventHubOptional = fabricSDK.withEventHub(id, endpoint);
            if (eventHubOptional.isPresent()) {
                return fabricSDK.attachEventHubToChain(eventHubOptional.get());
            }
        }
        return true;
    }

    private boolean reachable(String endpoint) {
        Socket s = null;
        try {
            s = new Socket();
            s.setReuseAddress(true);
            URI uri = new URI(endpoint);
            SocketAddress sa = new InetSocketAddress(uri.getHost(), uri.getPort());
            s.connect(sa, peerConnectTimeout);
            return true;
        } catch (Exception e) {
            LOG.error("failed to connect to {}: {}", endpoint, e.getMessage());
        } finally {
            if (s != null && s.isConnected()) {
                try {
                    s.close();
                } catch (IOException e) {
                    LOG.error("failed to close socket on {}: {}", endpoint, e.getMessage());
                }
            }
        }
        return false;
    }

    public boolean connectToPeer(String peerId) {
        return peerRepo
          .findPeerEventhubById(peerId)
          .map(peer -> reachable(peer.getEndpoint()))
          .orElse(false);
    }

    public Optional<PeerEventhub> enrollPeer(PeerEventhub peer) {
        try {
            /* admin users should be submitted to the system beforehand */
            Optional<PeerEventhub> userOptional = peerRepo.findPeerEventhubByIdAndPasswordAndAffiliation(peer.getId(), peer.getPassword(), peer.getAffiliation());
            return userOptional.map(u -> u.enrolled()
              ? u
              : fabricSDK
                .enroll(peer.getId(), peer.getPassword(), peer.getCa(), peer.getUri())
                .map(enrollment -> {
                    u.setCertificate(enrollment.getCert());
                    u.setPrivateKey(Base64
                      .getEncoder()
                      .encodeToString(enrollment
                        .getKey()
                        .getEncoded()));
                    LOG.info("peer {} enrolled, saving msp info...", u);
                    return peerRepo.save(u);
                })
                .orElse(null));

        } catch (Exception e) {
            LOG.error("failed to enroll peer {}: {}", peer, e.getMessage());
        }
        return empty();
    }
}
