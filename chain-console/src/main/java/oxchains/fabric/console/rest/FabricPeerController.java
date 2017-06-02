package oxchains.fabric.console.rest;

import org.springframework.web.bind.annotation.*;
import oxchains.fabric.console.domain.PeerEventhub;
import oxchains.fabric.console.domain.PeerInfo;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.console.service.PeerService;

import java.util.List;
import java.util.Optional;

import static oxchains.fabric.console.rest.common.RestResp.fail;
import static oxchains.fabric.console.rest.common.RestResp.success;

/**
 * @author aiet
 */
@RestController
public class FabricPeerController {

    private PeerService peerService;

    public FabricPeerController(PeerService peerService) {
        this.peerService = peerService;
    }

    @GetMapping("/peer")
    public RestResp list() {
        List<PeerInfo> peerInfoList = peerService.allPeers();
        return success(peerInfoList);
    }

    @DeleteMapping("/peer/{peerId}")
    public RestResp remove(@PathVariable String peerId){
        peerService.removePeer(peerId);
        return success(null);
    }

    @PostMapping("/peer")
    public RestResp addPeer(@RequestBody PeerEventhub peerEventhub) {
        boolean added = peerService.addPeer(peerEventhub);
        return added ? success(peerEventhub) : fail();
    }

    @PostMapping("/peer/{peerId}/connection")
    public RestResp connectPeerAndEventHub(@PathVariable String peerId) {
        boolean reachable = peerService.connectToPeer(peerId);
        return reachable ? success(null) : fail();
    }

    @PostMapping("/peer/enrollment")
    public RestResp enroll(@RequestBody PeerEventhub peer) {
        Optional<PeerEventhub> peerEnrollment = peerService.enrollPeer(peer);
        return peerEnrollment
          .map(RestResp::success)
          .orElseGet(RestResp::fail);
    }

}
