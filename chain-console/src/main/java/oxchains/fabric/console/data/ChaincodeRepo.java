package oxchains.fabric.console.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oxchains.fabric.console.domain.ChainCodeInfo;

/**
 * @author aiet
 */
@Repository
public interface ChaincodeRepo extends CrudRepository<ChainCodeInfo, String>{

    ChainCodeInfo findByNameAndVersion(String name, String version);
}
