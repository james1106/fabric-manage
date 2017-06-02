package oxchains.fabric.console.rest;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.domain.ChainBlockInfo;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.console.service.ChainService;

import java.util.Optional;

import static java.util.Objects.nonNull;
import static oxchains.fabric.console.rest.common.RestResp.fail;
import static oxchains.fabric.console.rest.common.RestResp.success;

/**
 * @author aiet
 */
@RestController
public class FabricChainController {

    private ChainService chainService;

    public FabricChainController(ChainService chainService) {
        this.chainService = chainService;
    }

    @PostMapping("/chain/{chainname}")
    public RestResp newChain(@PathVariable String chainname, @RequestParam("config") MultipartFile config) {
        return chainService.newChain(chainname, config) ? success(null) : fail();
    }

    @GetMapping("/chain/{chainname}")
    public RestResp chaininfo(@PathVariable String chainname) {
        return chainService
          .chainInfo(chainname)
          .map(RestResp::success)
          .orElse(fail());
    }

    @GetMapping("/chain/{chainname}/block")
    public RestResp chainblock(@PathVariable String chainname, @RequestParam(required = false, defaultValue = "-1") long number, @RequestParam(required = false) String tx) {
        Optional<ChainBlockInfo> blockInfoOptional;
        if (nonNull(tx)) {
            blockInfoOptional = chainService.chainBlockByTx(chainname, tx);
        } else if (number > -1) {
            blockInfoOptional = chainService.chainBlockByNumber(chainname, number);
        } else return success(chainService.chainBlocks(chainname));

        return blockInfoOptional
          .map(RestResp::success)
          .orElse(fail());
    }

    @GetMapping("/chain/{chainname}/tx/{tx}")
    public RestResp chainblock(@PathVariable String chainname, @PathVariable String tx) {
        return chainService
          .transaction(chainname, tx)
          .map(RestResp::success)
          .orElse(fail());
    }

    @PostMapping("/chain/{chainname}/peer/{peerId}")
    public RestResp joinChain(@PathVariable String chainname, @PathVariable String peerId){
        return chainService.joinChain(chainname, peerId) ? success(null) : fail() ;
    }

}
