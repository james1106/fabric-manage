package oxchains.fabric.console.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oxchains.fabric.console.domain.ChainCodeInfo;

import java.util.Optional;

/**
 * @author aiet
 */
@Repository
public interface ChaincodeRepo extends CrudRepository<ChainCodeInfo, String>{

    Optional<ChainCodeInfo> findByNameAndVersionAndAffiliation(String name, String version, String affiliation);

    Iterable<ChainCodeInfo> findByAffiliation(String affiliation);
}
