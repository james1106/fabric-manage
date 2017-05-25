package oxchains.fabric.console.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oxchains.fabric.console.auth.JwtService;
import oxchains.fabric.console.data.UserRepo;
import oxchains.fabric.console.data.UserTokenRepo;
import oxchains.fabric.console.domain.User;
import oxchains.fabric.console.domain.UserToken;
import oxchains.fabric.sdk.FabricSDK;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
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
    private JwtService jwtService;

    public UserService(@Autowired UserRepo userRepo, @Autowired UserTokenRepo userTokenRepo, @Autowired FabricSDK fabricSDK, @Autowired JwtService jwtService) {
        this.userRepo = userRepo;
        this.userTokenRepo = userTokenRepo;
        this.fabricSDK = fabricSDK;
        this.jwtService = jwtService;
    }

    public List<User> userList() {
        try {
            return newArrayList(userRepo.findAll());
        } catch (Exception e) {
            LOG.error("failed to fetch users: ", e);
        }
        return emptyList();
    }

    public List<User> userList(String affiliation) {
        try {
            return newArrayList(userRepo.findUsersByAffiliation(affiliation));
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

    public boolean revoke(String username, String affiliation, int reason) {
        try {
            boolean revoked = fabricSDK.revokeUser(username, affiliation, reason);
            if (revoked) {
                Optional<User> userOptional = userRepo.findUserByUsernameAndAffiliation(username, affiliation);
                if (userOptional.isPresent()) {
                    userRepo.delete(userOptional.get());
                    return true;
                }
            }
        } catch (Exception e) {
            LOG.error("failed to revoke user {}", username, e);
        }
        return false;
    }

    private UserToken generateUserToken(User user) {
        UserToken userToken = new UserToken(user, jwtService.generate(user));
        userTokenRepo.save(userToken);
        return userToken;
    }

    public Optional<UserToken> tokenForUser(final User user) {
        try {
            /* admin users should be submitted to the system beforehand */
            Optional<User> userOptional = userRepo.findUserByUsernameAndPasswordAndAffiliation(user.getUsername(), user.getPassword(), user.getAffiliation());
            return userOptional
              .map(u -> u.enrolled()
                ? u
                : fabricSDK
                  .enroll(user.getUsername(), user.getPassword(), user.getCa(), user.getUri())
                  .map(enrollment -> {
                      u.setCertificate(enrollment.getCert());
                      u.setPrivateKey(Base64
                        .getEncoder()
                        .encodeToString(enrollment
                          .getKey()
                          .getEncoded()));
                      LOG.info("user {} enrolled, saving msp info...");
                      return userRepo.save(u);
                  })
                  .orElse(null))
              .map(this::generateUserToken);
        } catch (Exception e) {
            LOG.error("failed to generate token for user {}: {}", user, e.getMessage());
        }
        return empty();
    }

}
