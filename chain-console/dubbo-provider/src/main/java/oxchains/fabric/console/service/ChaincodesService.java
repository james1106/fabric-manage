package oxchains.fabric.console.service;

import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.domain.ChainCodeInfo;
import oxchains.fabric.console.rest.common.QueryResult;
import oxchains.fabric.console.rest.common.TxPeerResult;
import oxchains.fabric.console.rest.common.TxResult;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Created by root on 17-7-28.
 */
public interface ChaincodesService {
    /**
     * 上传chaincode
     * @param chaincodeName
     * @param version
     * @param lang  "go" or "java"
     * @param chaincodeFile
     * @return
     */
    //public boolean cacheChaincode(String chaincodeName, String version, String lang, MultipartFile chaincodeFile);
    public boolean cacheChaincode(String chaincodeName, String version, String lang, byte[] chaincodeFile);

    /**
     * 安装chaincode
     * @param channelName
     * @param chaincodeName
     * @param version
     * @param lang
     * @param peers
     * @return
     */
    public List<TxResult> installCCOnPeer(String channelName, String chaincodeName, String version, String lang, String... peers);

    /**
     * 初始化chaincode
     * @param channelName
     * @param chaincodeName
     * @param version
     * @param endorsementFile
     * @param params
     * @return
     */
    //public Optional<TxPeerResult> instantiate(String channelName, String chaincodeName, String version, MultipartFile endorsementFile, String... params);
    public TxPeerResult instantiate(String channelName, String chaincodeName, String version, byte[] endorsementFile, String... params);

    /**
     * 返回chaincode信息
     * @return
     */
    public List<ChainCodeInfo> chaincodes();

    /**
     * 调用chaincode
     * @param channelName
     * @param chaincodeName
     * @param version
     * @param params
     * @return
     */
    //public Optional<TxPeerResult> invoke(String channelName, String chaincodeName, String version, String... params);
    public TxPeerResult invokecc(String channelName, String chaincodeName, String version, String... params);

    /**
     * 查询chaincode
     * @param channelName
     * @param chaincodeName
     * @param version
     * @param args
     * @return
     */
    //public Optional<QueryResult> query(String channelName, String chaincodeName, String version, String... args);
    public QueryResult querycc(String channelName, String chaincodeName, String version, String... args);


}
