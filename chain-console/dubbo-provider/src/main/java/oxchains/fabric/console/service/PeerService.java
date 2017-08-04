package oxchains.fabric.console.service;

import oxchains.fabric.console.domain.PeerEventhub;
import oxchains.fabric.console.domain.PeerInfo;

import java.util.List;
import java.util.Optional;

/**
 * Created by root on 17-7-28.
 */
public interface PeerService {
    public List<PeerInfo> allPeers();

    public boolean addPeer(PeerEventhub peerEventhub);

    public boolean connectToPeer(String peerId);

    public Optional<PeerEventhub> enrollPeer(PeerEventhub peer);

    public void removePeer(final String peerId);
}
