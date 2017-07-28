package oxchains.fabric.console.service;

import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.domain.ChainCodeInfo;
import oxchains.fabric.console.rest.common.QueryResult;
import oxchains.fabric.console.rest.common.TxPeerResult;
import oxchains.fabric.console.rest.common.TxResult;

import java.util.List;
import java.util.Optional;

/**
 * Created by root on 17-7-28.
 */
public interface ChaincodeService {
    public boolean cacheChaincode(String name, String version, String lang, MultipartFile file);

    public List<TxResult> installCCOnPeer(String chain, String name, String version, String lang, String... peers);

    public Optional<TxPeerResult> instantiate(String chain, String name, String version, MultipartFile endorsement, String... params);

    public List<ChainCodeInfo> chaincodes();

    public Optional<TxPeerResult> invoke(String chain, String name, String version, String... params);

    public Optional<QueryResult> query(String chain, String name, String version, String... args);


}
