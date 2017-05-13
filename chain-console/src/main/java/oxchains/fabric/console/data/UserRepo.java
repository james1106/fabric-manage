package oxchains.fabric.console.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oxchains.fabric.console.domain.User;

import java.util.Optional;

/**
 * @author aiet
 */
@Repository
public interface UserRepo extends CrudRepository<User, String>{

    Optional<User> findUserByUsernameAndPassword(String username, String password);

    Optional<User> findUserByUsername(String username);

}
