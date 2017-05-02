package oxchains.fabric.console.rest;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import oxchains.fabric.console.data.UserRepo;
import oxchains.fabric.console.data.UserTokenRepo;
import oxchains.fabric.console.domain.User;
import oxchains.fabric.console.domain.UserToken;
import oxchains.fabric.console.rest.common.RestResp;

import java.util.UUID;

import static java.util.Objects.isNull;
import static oxchains.fabric.console.rest.common.RestResp.fail;
import static oxchains.fabric.console.rest.common.RestResp.success;

/**
 * @author aiet
 */
@RestController
public class FabricUserController {

    private UserRepo userRepo;
    private UserTokenRepo userTokenRepo;

    public FabricUserController(
      @Autowired UserRepo userRepo,
      @Autowired UserTokenRepo userTokenRepo
    ) {
        this.userRepo = userRepo;
        this.userTokenRepo = userTokenRepo;
    }

    @GetMapping("/user")
    public RestResp list() {
        return success(Lists.newArrayList(userRepo.findAll()));
    }

    @PostMapping("/user")
    public RestResp register(@RequestBody User user) {
        User savedUser = userRepo.save(user);
        return success(savedUser);
    }

    @PutMapping("/user/{username}")
    public RestResp revoke(@PathVariable String username, @RequestParam int action) {
        if (action == 0) {
            User user = userRepo.findUserByUsername(username);
            userRepo.delete(user);
            return success(null);
        }
        return fail();
    }

    @PostMapping("/user/token")
    public RestResp enroll(@RequestBody User user) {
        User foundUser = userRepo.findUserByUsernameAndPassword(user.getUsername(), user.getPassword());
        if (isNull(foundUser)) return fail();
        else {
            UserToken userToken = new UserToken(foundUser, UUID.randomUUID().toString());
            UserToken token = userTokenRepo.save(userToken);
            return success(token);
        }
    }

}
