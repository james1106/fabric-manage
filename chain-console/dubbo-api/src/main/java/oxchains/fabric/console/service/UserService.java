package oxchains.fabric.console.service;

import oxchains.fabric.console.domain.User;
import oxchains.fabric.console.domain.UserToken;

import java.util.List;
import java.util.Optional;

/**
 * Created by root on 17-7-28.
 */
public interface UserService {
    public List<User> userList();

    public Optional<User> register(User user);

    public boolean revoke(String username, int reason);

    public Optional<UserToken> tokenForUser(final User user);
}
