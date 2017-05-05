package oxchains.fabric.console.service;

import org.hyperledger.fabric.sdk.Enrollment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oxchains.fabric.console.data.UserRepo;
import oxchains.fabric.console.data.UserTokenRepo;
import oxchains.fabric.console.domain.User;
import oxchains.fabric.console.domain.UserToken;
import oxchains.fabric.sdk.FabricSDK;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;

/**
 * @author aiet
 */
@Service
public class UserService {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private UserRepo userRepo;
    private UserTokenRepo userTokenRepo;
    private FabricSDK fabricSDK;

    public UserService(@Autowired UserRepo userRepo, @Autowired UserTokenRepo userTokenRepo, @Autowired FabricSDK fabricSDK) {
        this.userRepo = userRepo;
        this.userTokenRepo = userTokenRepo;
        this.fabricSDK = fabricSDK;
    }

    public List<User> userList() {
        try {
            return newArrayList(userRepo.findAll());
        } catch (Exception e) {
            LOG.error("failed to fetch users: ", e);
        }
        return emptyList();
    }

    public Optional<User> register(User user) {
        try {
            boolean registered = fabricSDK.register(user.getUsername(), user.getAffiliation(), user.getPassword());
            if (registered) return Optional.of(userRepo.save(user));
        } catch (Exception e) {
            LOG.error("failed to register user {}:", user, e);
        }
        return empty();
    }

    public boolean revoke(String username, int reason) {
        try {
            boolean revoked = fabricSDK.revokeUser(username, reason);
            if (revoked) {
                User user = userRepo.findUserByUsername(username);
                userRepo.delete(user);
                return true;
            }
        } catch (Exception e) {
            LOG.error("failed to revoke user {}", username, e);
        }
        return false;
    }

    public Optional<UserToken> tokenForUser(User user) {
        try {
            User foundUser = userRepo.findUserByUsernameAndPassword(user.getUsername(), user.getPassword());
            if (nonNull(foundUser)) {
                Optional<Enrollment> enrollmentOptional = fabricSDK.enroll(user.getUsername(), user.getPassword());
                if(enrollmentOptional.isPresent()) {
                    UserToken userToken = new UserToken(foundUser, UUID
                      .randomUUID()
                      .toString());
                    //TODO what to do with the enrollment keys?
                    return Optional.of(userTokenRepo.save(userToken));
                }
            }
        } catch (Exception e) {
            LOG.error("failed to generate token for user {}", user, e);
        }
        return empty();
    }

}
