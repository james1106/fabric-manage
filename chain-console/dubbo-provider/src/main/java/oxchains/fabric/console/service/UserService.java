package oxchains.fabric.console.service;

import oxchains.fabric.console.domain.JwtAuth;
import oxchains.fabric.console.domain.User;
import oxchains.fabric.console.domain.UserToken;

import java.util.List;

/**
 * Created by root on 17-7-28.
 */
public interface UserService {
    public List<User> userList();

    //public Optional<User> register(User user);

    public User registerUser(User user);

    public boolean revoke(String username, int reason);

    //public Optional<UserToken> tokenForUser(final User user);
    public UserToken enrollUser(User user);

    public JwtAuth parseToken(String token);
}
