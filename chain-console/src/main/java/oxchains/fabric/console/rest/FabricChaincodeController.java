package oxchains.fabric.console.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.console.rest.common.TxResult;
import oxchains.fabric.console.service.ChaincodeService;

import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static oxchains.fabric.console.rest.common.RestResp.fail;
import static oxchains.fabric.console.rest.common.RestResp.success;

/**
 * @author aiet
 */
@RestController
public class FabricChaincodeController {

    private ChaincodeService chaincodeService;

    public FabricChaincodeController(@Autowired ChaincodeService chaincodeService) {
        this.chaincodeService = chaincodeService;
    }

    @PostMapping(value = "/chaincode/file", consumes = MULTIPART_FORM_DATA_VALUE)
    public RestResp upload(@RequestParam("chaincode") MultipartFile file, @RequestParam String name, @RequestParam String version, @RequestParam(required = false, defaultValue = "go") String lang) {
        boolean saved = chaincodeService.cacheChaincode(name, version, lang, file);
        return saved ? success(null) : fail();
    }

    @PostMapping("/chaincode/install/{chaincode}")
    public RestResp install(@PathVariable String chaincode, @RequestParam(required = false, defaultValue = "1.0") String version, @RequestParam(required = false, defaultValue = "go") String lang, @RequestParam(required = false) String[] peers) {
        List<TxResult> result = chaincodeService.installCCOnPeer(chaincode, version, lang, peers);
        return result.isEmpty() ? fail() : success(result);
    }

    @PostMapping(value = "/chaincode", consumes = MULTIPART_FORM_DATA_VALUE)
    public RestResp init(@RequestParam MultipartFile endorsement, @RequestParam String name, @RequestParam String version, @RequestParam String[] args) {
        return chaincodeService
          .instantiate(name, version, args, endorsement)
          .map(RestResp::success)
          .orElse(fail());
    }

    @GetMapping("/chaincode")
    public RestResp list() {
        return success(chaincodeService.chaincodes());
    }

    @PostMapping("/chaincode/tx/{chaincode}/{version}")
    public RestResp commit(@PathVariable String chaincode, @PathVariable String version, @RequestParam String[] args) {
        return chaincodeService
          .invoke(chaincode, version, args)
          .map(RestResp::success)
          .orElse(fail());
    }

    @GetMapping("/chaincode/tx/{chaincode}/{version}")
    public RestResp query(@PathVariable String chaincode, @PathVariable String version, @RequestParam String[] args) {
        return chaincodeService
          .query(chaincode, version, args)
          .map(RestResp::success)
          .orElse(fail());
    }

}
