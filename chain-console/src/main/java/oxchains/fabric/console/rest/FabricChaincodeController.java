package oxchains.fabric.console.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.console.rest.common.TxResult;
import oxchains.fabric.console.service.ChaincodeService;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

    @PostMapping("/chaincode/install")
    public RestResp install(@RequestParam String chain, @RequestParam String chaincode, @RequestParam String version, @RequestParam String lang, @RequestParam String[] peers) {
        List<TxResult> result = chaincodeService.installCCOnPeer(chain, chaincode, version, lang, peers);
        return result.isEmpty() ? fail() : success(result);
    }

    @PostMapping(value = "/chaincode", consumes = MULTIPART_FORM_DATA_VALUE)
    public RestResp init(@RequestParam String chain, @RequestParam MultipartFile endorsement, @RequestParam String name, @RequestParam String version, @RequestParam String[] args) {
        return chaincodeService
          .instantiate(chain, name, version, endorsement, args)
          .map(RestResp::success)
          .orElse(fail());
    }

    @GetMapping("/chaincode")
    public RestResp list() {
        return success(chaincodeService.chaincodes());
    }

    @PostMapping("/chaincode/tx")
    public RestResp commit(@RequestParam String chain, @RequestParam String chaincode, @RequestParam String version, @RequestParam String[] args) {
        return chaincodeService
          .invoke(chain, chaincode, version, args)
          .map(result -> {
              if (result.getSuccess() == 1) {
                  return RestResp.success(result);
              } else {
                  return RestResp.fail("invocation failed", result);
              }
          })
          .orElse(fail());
    }

    @GetMapping("/chaincode/tx")
    public RestResp query(@RequestParam String chain, @RequestParam String chaincode, @RequestParam String version, @RequestParam String[] args) {

        return chaincodeService
          .query(chain, chaincode, version, args)
          .map(RestResp::success)
          .orElse(fail());
    }

}
