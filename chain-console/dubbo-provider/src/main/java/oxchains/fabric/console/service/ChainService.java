package oxchains.fabric.console.service;

import org.hyperledger.fabric.sdk.Channel;
import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.domain.ChainBlockInfo;
import oxchains.fabric.console.domain.ChainInfo;
import oxchains.fabric.console.domain.EventHubInfo;
import oxchains.fabric.console.domain.TxInfo;

import java.util.List;
import java.util.Optional;

/**
 * Created by root on 17-7-28.
 */
public interface ChainService {
    public Optional<ChainInfo> chainInfo(String chainname);

    public List<ChainBlockInfo> chainBlocks(String chainname);

    public Optional<ChainBlockInfo> chainBlockByNumber(String chainname, long block);

    public Optional<ChainBlockInfo> chainBlockByTx(String chainname, String tx);

    public Optional<TxInfo> transaction(String chainname, String tx);

    public List<EventHubInfo> eventHubs(String chainname);

    //public boolean newChain(String chain, MultipartFile config);
    public boolean newChain(String chain, byte[] config);

    public boolean joinChain(String chainname, String peerId);

    public List<ChainInfo> chains();
}
