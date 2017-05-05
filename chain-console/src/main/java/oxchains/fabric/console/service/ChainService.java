package oxchains.fabric.console.service;

import org.springframework.stereotype.Service;
import oxchains.fabric.console.domain.ChainBlockInfo;
import oxchains.fabric.console.domain.ChainInfo;
import oxchains.fabric.console.domain.TxInfo;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.sdk.FabricSDK;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * @author aiet
 */
@Service
public class ChainService {

    private FabricSDK fabricSDK;

    public ChainService(FabricSDK fabricSDK) {
        this.fabricSDK = fabricSDK;
    }

    public Optional<ChainInfo> chaininfo() {
        return fabricSDK
          .getChaininfo()
          .map(ChainInfo::new);
    }

    public List<ChainBlockInfo> chainblocks() {
        return fabricSDK
          .getChainBlocks()
          .stream()
          .map(ChainBlockInfo::new)
          .collect(toList());
    }

    public Optional<ChainBlockInfo> chainBlockByNumber(long block) {
        return fabricSDK.getChainBlock(block).map(ChainBlockInfo::new);
    }

    public Optional<ChainBlockInfo> chainBlockByTx(String tx) {
        return fabricSDK.getChainBlock(tx).map(ChainBlockInfo::new);
    }

    public Optional<TxInfo> transaction(String tx) {
        return fabricSDK.getChainTx(tx).map(TxInfo::new);
    }

}
