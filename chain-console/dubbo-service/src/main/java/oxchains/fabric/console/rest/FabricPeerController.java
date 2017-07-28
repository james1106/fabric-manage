package oxchains.fabric.console.rest;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;
import oxchains.fabric.console.domain.PeerEventhub;
import oxchains.fabric.console.domain.PeerInfo;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.console.service.PeerServiceImpl;

import java.util.List;
import java.util.Optional;

import static oxchains.fabric.console.rest.common.RestResp.fail;
import static oxchains.fabric.console.rest.common.RestResp.success;

/**
 * @author aiet
 */
@RestController
public class FabricPeerController {

    @Reference(version = "1.0.0")
    private PeerServiceImpl peerServiceImpl;

    public FabricPeerController(PeerServiceImpl peerServiceImpl) {
        this.peerServiceImpl = peerServiceImpl;
    }

    @GetMapping("/peer")
    public RestResp list() {
        List<PeerInfo> peerInfoList = peerServiceImpl.allPeers();
        return success(peerInfoList);
    }

    @DeleteMapping("/peer/{peerId}")
    public RestResp remove(@PathVariable String peerId){
        peerServiceImpl.removePeer(peerId);
        return success(null);
    }

    @PostMapping("/peer")
    public RestResp addPeer(@RequestBody PeerEventhub peerEventhub) {
        boolean added = peerServiceImpl.addPeer(peerEventhub);
        return added ? success(peerEventhub) : fail();
    }

    @PostMapping("/peer/{peerId}/connection")
    public RestResp connectPeerAndEventHub(@PathVariable String peerId) {
        boolean reachable = peerServiceImpl.connectToPeer(peerId);
        return reachable ? success(null) : fail();
    }

    @PostMapping("/peer/enrollment")
    public RestResp enroll(@RequestBody PeerEventhub peer) {
        Optional<PeerEventhub> peerEnrollment = peerServiceImpl.enrollPeer(peer);
        return peerEnrollment
          .map(RestResp::success)
          .orElseGet(RestResp::fail);
    }

}
