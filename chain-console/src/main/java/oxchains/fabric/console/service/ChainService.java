package oxchains.fabric.console.service;

import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.auth.JwtAuthentication;
import oxchains.fabric.console.data.PeerRepo;
import oxchains.fabric.console.domain.*;
import oxchains.fabric.console.rest.common.RestResp;
import oxchains.fabric.sdk.FabricSDK;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static oxchains.fabric.sdk.domain.CAUser.fromUser2;

/**
 * @author aiet
 */
@Service
public class ChainService {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    private FabricSDK fabricSDK;
    private PeerRepo peerRepo;

    public ChainService(@Autowired FabricSDK fabricSDK, @Autowired PeerRepo peerRepo) {
        this.fabricSDK = fabricSDK;
        this.peerRepo = peerRepo;
    }

    public Optional<ChainInfo> chainInfo(String chainname) {
        if (noPeersYet(chainname)) return empty();
        else return fabricSDK
          .getChaininfo(chainname)
          .map(ChainInfo::new);
    }

    public List<ChainBlockInfo> chainBlocks(String chainname) {
        if (noPeersYet(chainname)) return emptyList();
        else return fabricSDK
          .getChainBlocks(chainname)
          .stream()
          .map(ChainBlockInfo::new)
          .collect(toList());
    }

    private boolean noPeersYet(String chainname) {
        return userContext()
          .map(u -> fabricSDK
            .withUserContext(fromUser2(u))
            .chainPeers(chainname))
          .orElse(emptyList())
          .isEmpty();
    }

    public Optional<ChainBlockInfo> chainBlockByNumber(String chainname, long block) {
        if (noPeersYet(chainname)) return empty();
        else return fabricSDK
          .getChainBlock(chainname, block)
          .map(ChainBlockInfo::new);
    }

    public Optional<ChainBlockInfo> chainBlockByTx(String chainname, String tx) {
        if (noPeersYet(chainname)) return empty();
        else return fabricSDK
          .getChainBlock(chainname, tx)
          .map(ChainBlockInfo::new);
    }

    public Optional<TxInfo> transaction(String chainname, String tx) {
        if (noPeersYet(chainname)) return empty();
        else return fabricSDK
          .getChainTx(chainname, tx)
          .map(TxInfo::new);
    }

    public List<EventHubInfo> eventHubs(String chainname) {
        return fabricSDK
          .chainEventHubs(chainname)
          .stream()
          .map(EventHubInfo::new)
          .collect(toList());
    }

    private Optional<User> userContext() {
        return ((JwtAuthentication) getContext().getAuthentication()).user();
    }

    public boolean newChain(String chain, MultipartFile config) {
        try {
            ChannelConfiguration chainConfiguration = new ChannelConfiguration(config.getBytes());
            LOG.info("constructing chain {}", chain);
            return userContext()
              .flatMap(context -> fabricSDK
                .withUserContext(fromUser2(context))
                .constructChain(chain, chainConfiguration))
              .isPresent();
        } catch (Exception e) {
            RestResp.set("failed to construct chain :"+e.getMessage());
            LOG.error("failed to construct chain {}", e.getMessage());
        }
        return false;

    }

    public boolean joinChain(String chainname, String peerId) {
        return peerRepo
          .findPeerEventhubById(peerId)
          .flatMap(peerEventhub -> userContext().map(context ->
            fabricSDK.withUserContext(fromUser2(context)).joinChain(peerEventhub, chainname)
          )).orElse(false);
    }

    public List<ChainInfo> chains() {
        return userContext()
          .map(context -> fabricSDK
            .withUserContext(fromUser2(context))
            .chains())
          .orElse(emptyList());
    }
}
