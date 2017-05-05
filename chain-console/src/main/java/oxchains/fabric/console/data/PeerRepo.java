package oxchains.fabric.console.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oxchains.fabric.console.domain.PeerEventhub;

/**
 * @author aiet
 */
@Repository
public interface PeerRepo extends CrudRepository<PeerEventhub, String>{
}
