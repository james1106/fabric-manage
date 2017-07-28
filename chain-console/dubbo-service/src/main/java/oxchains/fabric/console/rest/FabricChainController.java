package oxchains.fabric.console.rest;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.domain.ChainBlockInfo;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.console.service.ChainServiceImpl;

import java.util.Optional;

import static java.util.Objects.nonNull;
import static oxchains.fabric.console.rest.common.RestResp.fail;
import static oxchains.fabric.console.rest.common.RestResp.success;

/**
 * @author aiet
 */
@RestController
public class FabricChainController {

    @Reference(version = "1.0.0")
    private ChainServiceImpl chainServiceImpl;

    public FabricChainController(ChainServiceImpl chainServiceImpl) {
        this.chainServiceImpl = chainServiceImpl;
    }

    @PostMapping("/chain")
    public RestResp newChain(@RequestParam String chainname, @RequestParam("config") MultipartFile config) {
        return chainServiceImpl.newChain(chainname, config) ? success(null) : fail();
    }

    @GetMapping("/chain")
    public RestResp chains(){
        return success(chainServiceImpl.chains());
    }

    @GetMapping("/chain/{chainname}")
    public RestResp chaininfo(@PathVariable String chainname) {
        return chainServiceImpl
          .chainInfo(chainname)
          .map(RestResp::success)
          .orElse(fail());
    }

    @GetMapping("/chain/{chainname}/block")
    public RestResp chainblock(@PathVariable String chainname, @RequestParam(required = false, defaultValue = "-1") long number, @RequestParam(required = false) String tx) {
        Optional<ChainBlockInfo> blockInfoOptional;
        if (nonNull(tx)) {
            blockInfoOptional = chainServiceImpl.chainBlockByTx(chainname, tx);
        } else if (number > -1) {
            blockInfoOptional = chainServiceImpl.chainBlockByNumber(chainname, number);
        } else return success(chainServiceImpl.chainBlocks(chainname));

        return blockInfoOptional
          .map(RestResp::success)
          .orElse(fail());
    }

    @GetMapping("/chain/{chainname}/tx/{tx}")
    public RestResp chainblock(@PathVariable String chainname, @PathVariable String tx) {
        return chainServiceImpl
          .transaction(chainname, tx)
          .map(RestResp::success)
          .orElse(fail());
    }

    @PostMapping("/chain/{chainname}/peer")
    public RestResp joinChain(@PathVariable String chainname, @RequestParam String peer){
        return chainServiceImpl.joinChain(chainname, peer) ? success(null) : fail() ;
    }

}
