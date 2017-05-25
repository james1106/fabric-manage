package oxchains.fabric.console.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import oxchains.fabric.console.data.UserRepo;

/**
 * @author aiet
 */
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final UserRepo userRepo;

    @Autowired
    public JwtAuthenticationProvider(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication != null && authentication.isAuthenticated()) {
            authentication.setAuthenticated(userRepo
              .findUserByUsernameAndAffiliation(authentication.getName(), (String) authentication.getDetails())
              .isPresent());
        }
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(JwtAuthentication.class);
    }

}
