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

    Optional<User> findUserByUsernameAndPasswordAndAffiliation(String username, String password, String affiliation);

    Optional<User> findUserByUsernameAndAffiliation(String username, String affiliation);

    Iterable<User> findUsersByAffiliation(String affiliation);
}
