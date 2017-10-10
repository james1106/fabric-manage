package oxchains.fabric.console.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import oxchains.fabric.console.domain.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author aiet
 */
public class JwtAuthentication implements Authentication {

    private String token;
    private User user;
    private Map<String, Object> details;

    JwtAuthentication(User user, String token, Map<String, Object> details) {
        this.user = user;
        this.token = token;
        this.details = details;
    }

    public Optional<User> user(){
        return Optional.ofNullable(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        return user != null && user.getCertificate() != null;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (!isAuthenticated) user = null;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    @Override
    public String toString() {
        return token;
    }
}
