package oxchains.fabric.console.rest;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.hibernate.hql.internal.ast.tree.RestrictableStatement;
import org.hyperledger.fabric.protos.peer.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.domain.ChainCodeInfo;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.console.rest.common.TxResult;
import oxchains.fabric.console.service.ChaincodeService;

import javax.annotation.Resource;
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

    @PostMapping("chaincode/install")
    public RestResp install (@RequestBody ChainCodeInfo chainCodeInfo) {
        List<TxResult> result = chaincodeService.installCCOnPeer(chainCodeInfo);
        return result.isEmpty() ? fail() : success(result);
    }

    @PostMapping("/chaincode/upgrade")
    public RestResp upGrade(@RequestParam String chain, @RequestParam String version, @RequestParam String name, @RequestParam MultipartFile endorsement, @RequestParam String[] args) {
        return chaincodeService
                .upgradeChainCode(chain, version, name, endorsement, args)
                .map(RestResp::success)
                .orElse(fail());
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
    public RestResp commit(@RequestBody ChainCodeInfo chainCodeInfo) {
        return chaincodeService.invoke(chainCodeInfo).map(result -> {
            if (result.getSuccess() == 1) {
                return RestResp.success("invocation successful", result);
            } else {
                return RestResp.fail("invocation failed", result);
            }
        }).orElse(fail());
    }

    @PostMapping("/chaincode/query")
    public RestResp query(@RequestBody ChainCodeInfo chainCodeInfo) {
        return chaincodeService.query(chainCodeInfo).map(RestResp::success).orElse(fail());
    }

    //*****************************参数获取方式@RequestBody之前的代码*****************************//


    /*@PostMapping("/chaincode/install")
    public RestResp install(@RequestParam String chain, @RequestParam String chaincode, @RequestParam String version, @RequestParam String lang, @RequestParam String[] peers) {
        List<TxResult> result = chaincodeService.installCCOnPeer(chain, chaincode, version, lang, peers);
        return result.isEmpty() ? fail() : success(result);
    }*/

    /*@PostMapping("/chaincode/tx")
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
    }*/

    /*@GetMapping("/chaincode/tx")
    public RestResp query(@RequestParam String chain, @RequestParam String chaincode, @RequestParam String version, @RequestParam String[] args) {
        return chaincodeService
                .query(chain, chaincode, version, args)
                .map(RestResp::success)
                .orElse(fail());
    }*/


    //*****************************参数获取方式为@RequestBody，但传递不是json数据*****************************//

    /*@PostMapping(value = "/chaincode", consumes = MULTIPART_FORM_DATA_VALUE)
    public RestResp init(@ModelAttribute ChainCodeInfo chainCodeInfo) {
        return chaincodeService.instantiate(chainCodeInfo).map(RestResp::success).orElse(fail());
    }*/


}
