package oxchains.fabric.console.data;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oxchains.fabric.console.domain.PeerEventhub;

import java.util.Optional;

/**
 * @author aiet
 */
@Repository
public interface PeerRepo extends CrudRepository<PeerEventhub, String>{

    Optional<PeerEventhub> findPeerEventhubById(String peerId);

    Iterable<PeerEventhub> findPeerEventhubsByAffiliation(String affiliation);

    Optional<PeerEventhub> findPeerEventhubByIdAndPasswordAndAffiliation(String id, String password, String affiliation);
}
