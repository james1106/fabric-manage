package oxchains.fabric.console.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import oxchains.fabric.console.domain.User;
import oxchains.fabric.console.domain.UserToken;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.console.service.UserService;

import java.util.Optional;

import static oxchains.fabric.console.rest.common.RestResp.fail;
import static oxchains.fabric.console.rest.common.RestResp.success;

/**
 * @author aiet
 */
@RestController
public class FabricUserController {

    private UserService userService;

    public FabricUserController(@Autowired UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public RestResp list() {
        return success(userService.userList());
    }

    @PostMapping("/user")
    public RestResp register(@RequestBody User user) {
        Optional<User> savedUser = userService.register(user);
        return savedUser
          .map(RestResp::success)
          .orElseGet(RestResp::fail);
    }

    @PutMapping("/user/{username}")
    public RestResp revoke(@PathVariable String username, @RequestParam int action, @RequestParam int reason) {
        if (action == 0) {
            boolean revoked = userService.revoke(username, reason);
            if (revoked) return success(null);
        }
        return fail();
    }

    @PostMapping("/user/token")
    public RestResp enroll(@RequestBody User user) {
        Optional<UserToken> userTokenOptional = userService.tokenForUser(user);
        return userTokenOptional
          .map(RestResp::success)
          .orElseGet(RestResp::fail);
    }

}
