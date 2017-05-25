package oxchains.fabric.console.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oxchains.fabric.console.domain.ChainInfo;

import java.util.Optional;

/**
 * @author aiet
 */
@Repository
public interface ChainRepo extends CrudRepository<ChainInfo, Long>{

    Optional<ChainInfo> findByNameAndOrderer(String name, String orderer);

}
