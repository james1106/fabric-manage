package oxchains.fabric.console.service;

import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import oxchains.fabric.console.domain.ChaincodeInfo;
import oxchains.fabric.console.domain.PeerInfo;
import oxchains.fabric.sdk.FabricSDK;

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

    public PeerService(FabricSDK fabricSDK) {
        this.fabricSDK = fabricSDK;
    }

    public List<PeerInfo> allPeers() {
        try {
            List<Peer> peers = fabricSDK.chainPeers();
            return peers
              .stream()
              .map(peer -> {
                  PeerInfo peerInfo = new PeerInfo(peer);
                  peerInfo.setChaincodes(fabricSDK
                    .chaincodesOnPeer(peer)
                    .stream()
                    .map(ChaincodeInfo::new)
                    .collect(toList()));
                  peerInfo.setChains(newArrayList(fabricSDK.chainsOfPeer(peer)));
                  return peerInfo;
              })
              .collect(toList());
        } catch (Exception e) {
            LOG.error("failed to fetch peers:", e);
        }
        return emptyList();
    }

    public boolean start(String peerId) {
        return false;
    }

    public boolean stop(String peerId) {
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



}
