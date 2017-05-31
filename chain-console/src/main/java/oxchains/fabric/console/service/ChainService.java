package oxchains.fabric.console.service;

import org.springframework.stereotype.Service;
import oxchains.fabric.console.domain.ChainBlockInfo;
import oxchains.fabric.console.domain.ChainInfo;
import oxchains.fabric.console.domain.EventHubInfo;
import oxchains.fabric.console.domain.TxInfo;
import oxchains.fabric.sdk.FabricSDK;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
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
        if (noPeersYet()) return empty();
        else return fabricSDK
          .getChaininfo()
          .map(ChainInfo::new);
    }

    public List<ChainBlockInfo> chainblocks() {
        if (noPeersYet()) return emptyList();
        else return fabricSDK
          .getChainBlocks()
          .stream()
          .map(ChainBlockInfo::new)
          .collect(toList());
    }

    private boolean noPeersYet() {
        return fabricSDK
          .chainPeers()
          .isEmpty();
    }

    public Optional<ChainBlockInfo> chainBlockByNumber(long block) {
        if (noPeersYet()) return empty();
        else return fabricSDK
          .getChainBlock(block)
          .map(ChainBlockInfo::new);
    }

    public Optional<ChainBlockInfo> chainBlockByTx(String tx) {
        if (noPeersYet()) return empty();
        else return fabricSDK
          .getChainBlock(tx)
          .map(ChainBlockInfo::new);
    }

    public Optional<TxInfo> transaction(String tx) {
        if (noPeersYet()) return empty();
        else return fabricSDK
          .getChainTx(tx)
          .map(TxInfo::new);
    }

    public List<EventHubInfo> eventhubs() {
        return fabricSDK
          .chainEventHubs()
          .stream()
          .map(EventHubInfo::new)
          .collect(toList());
    }

}
