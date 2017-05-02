package oxchains.fabric.console.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oxchains.fabric.console.domain.UserToken;

/**
 * @author aiet
 */
@Repository
public interface UserTokenRepo extends CrudRepository<UserToken, String>{
}
