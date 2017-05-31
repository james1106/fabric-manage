package oxchains.fabric.console.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @GetMapping("/chain")
    public RestResp chaininfo() {
        return chainService
          .chaininfo()
          .map(RestResp::success)
          .orElse(fail());
    }

    @GetMapping("/chain/block")
    public RestResp chainblock(@RequestParam(required = false, defaultValue = "-1") long number, @RequestParam(required = false) String tx) {
        Optional<ChainBlockInfo> blockInfoOptional;
        if (nonNull(tx)) {
            blockInfoOptional = chainService.chainBlockByTx(tx);
        } else if (number > -1) {
            blockInfoOptional = chainService.chainBlockByNumber(number);
        } else return success(chainService.chainblocks());

        return blockInfoOptional
          .map(RestResp::success)
          .orElse(fail());
    }

    @GetMapping("/chain/tx/{tx}")
    public RestResp chainblock(@PathVariable String tx) {
        return chainService
          .transaction(tx)
          .map(RestResp::success)
          .orElse(fail());
    }

    @GetMapping("/peer/eventhub")
    public RestResp eventhubs() {
        return success(chainService.eventhubs());
    }


}
