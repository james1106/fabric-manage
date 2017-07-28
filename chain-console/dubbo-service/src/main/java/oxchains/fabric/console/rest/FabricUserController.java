package oxchains.fabric.console.rest;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;
import oxchains.fabric.console.domain.User;
import oxchains.fabric.console.domain.UserToken;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.console.service.UserServiceImpl;

import java.util.Optional;

import static oxchains.fabric.console.rest.common.RestResp.fail;
import static oxchains.fabric.console.rest.common.RestResp.success;

/**
 * @author aiet
 */
@RestController
public class FabricUserController {

    @Reference(version = "1.0.0")
    private UserServiceImpl userServiceImpl;

    public FabricUserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping("/user")
    public RestResp list() {
        return success(userServiceImpl.userList());
    }

    @PostMapping("/user")
    public RestResp register(@RequestBody User user) {
        Optional<User> savedUser = userServiceImpl.register(user);
        return savedUser
          .map(RestResp::success)
          .orElseGet(RestResp::fail);
    }

    @PutMapping("/user/{username}")
    public RestResp revoke(@PathVariable String username, @RequestParam int action, @RequestParam int reason) {
        if (action == 0) {
            boolean revoked = userServiceImpl.revoke(username, reason);
            if (revoked) return success(null);
        }
        return fail();
    }

    @PostMapping("/user/token")
    public RestResp enroll(@RequestBody User user) {
        Optional<UserToken> userTokenOptional = userServiceImpl.tokenForUser(user);
        return userTokenOptional
          .map(RestResp::success)
          .orElseGet(RestResp::fail);
    }

}
