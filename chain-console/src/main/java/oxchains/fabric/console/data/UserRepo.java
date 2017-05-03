package oxchains.fabric.console.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oxchains.fabric.console.domain.User;

/**
 * @author aiet
 */
@Repository
public interface UserRepo extends CrudRepository<User, String>{

    User findUserByUsernameAndPassword(String username, String password);

    User findUserByUsername(String username);

}
