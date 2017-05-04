package oxchains.fabric.console.rest;

import org.springframework.web.bind.annotation.*;
import oxchains.fabric.console.domain.PeerInfo;
import oxchains.fabric.console.rest.common.PeerEventhub;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.console.service.PeerService;

import java.util.List;

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

    @PutMapping("/peer/{peerId}/status")
    public RestResp changePeerStatus(@PathVariable String peerId, @RequestParam int action) {
        boolean operationDone = false;
        switch (action) {
        case 1:
            operationDone = peerService.start(peerId);
            break;
        case 0:
            operationDone = peerService.stop(peerId);
        default:
            break;
        }
        return operationDone ? fail() : success(null);
    }

    @PostMapping("/peer")
    public RestResp addPeer(@RequestBody PeerEventhub peerEventhub) {
        boolean added = peerService.addPeer(peerEventhub.getId(), peerEventhub.getEndpoint(), peerEventhub.getEventhub());
        return added ? success(peerEventhub) : fail();
    }

}
