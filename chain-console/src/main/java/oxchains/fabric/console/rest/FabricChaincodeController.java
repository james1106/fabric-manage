package oxchains.fabric.console.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import oxchains.fabric.console.rest.common.RestResp;

import static oxchains.fabric.console.rest.common.RestResp.fail;

/**
 * @author aiet
 */
@RestController
public class FabricChaincodeController {

    @PostMapping("/chaincode")
    public RestResp upload(){
        return fail();
    }

    @PutMapping("/chaincode")
    public RestResp init(){
        return fail();
    }

    @GetMapping("/chaincode")
    public RestResp list(){
        return fail();
    }

    @PostMapping("/chaincode/tx")
    public RestResp commit(){
        return fail();
    }

    @GetMapping("/chaincode/tx")
    public RestResp query(){
        return fail();
    }

}
