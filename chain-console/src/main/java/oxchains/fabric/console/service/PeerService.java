package oxchains.fabric.console.service;

import com.google.common.collect.Streams;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oxchains.fabric.console.domain.ChaincodeInfo;
import oxchains.fabric.console.domain.PeerInfo;
import oxchains.fabric.sdk.FabricSDK;
import oxchains.fabric.sdk.FabricSSH;
import oxchains.fabric.sdk.FabricSSH.SSHResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

/**
 * @author aiet
 */
@Service
public class PeerService {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private FabricSDK fabricSDK;
    private FabricSSH fabricSSH;

    public PeerService(@Autowired FabricSDK fabricSDK, @Autowired FabricSSH fabricSSH) {
        this.fabricSDK = fabricSDK;
        this.fabricSSH = fabricSSH;
    }

    public List<PeerInfo> allPeers() {
        try {
            List<Peer> peers = fabricSDK.chainPeers();
            return peers
              .parallelStream()
              .map(peer -> {
                  PeerInfo peerInfo = new PeerInfo(peer);
                  peerInfo.setChaincodes(fabricSDK
                    .chaincodesOnPeer(peer)
                    .stream()
                    .map(ChaincodeInfo::new)
                    .collect(toList()));
                  peerInfo.setChains(newArrayList(fabricSDK.chainsOfPeer(peer)));
                  peerInfo.setStatusCode(reachable(peer.getUrl())?1:0);
                  return peerInfo;
              })
              .collect(toList());
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
        Optional<SSHResponse> responseOptional = fabricSSH.stopPeer(peerId);
        if (responseOptional.isPresent()) return responseOptional
          .map(SSHResponse::succeeded)
          .orElse(false);
        return false;
    }

    public boolean addPeer(String peerId, String peerEndpoint, String eventhubEndpoint) {
        try {
            Optional<Peer> peerOptional = fabricSDK.withPeer(peerId, peerEndpoint);
            boolean eventHubAttached = eventHubAttached(peerId, eventhubEndpoint);
            boolean peerJoined = peerOptional.isPresent() && fabricSDK.joinChain(peerOptional.get());
            return eventHubAttached && peerJoined;
        } catch (Exception e) {
            LOG.error("failed to add peer {}@{} #{}", peerId, peerEndpoint, eventhubEndpoint, e);
        }
        return false;
    }

    private boolean eventHubAttached(String id, String endpoint) {
        if (!isNull(endpoint)) {
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
            s.connect(sa, 3000);
            return true;
        } catch (Exception e) {
            LOG.error("failed to connect to {}", endpoint, e);
        } finally {
            if (s != null && s.isConnected()) {
                try {
                    s.close();
                } catch (IOException e) {
                    LOG.error("failed to close socket on {}", endpoint, e);
                }
            }
        }
        return false;
    }

}
