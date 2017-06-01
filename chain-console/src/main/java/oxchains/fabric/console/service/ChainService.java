package oxchains.fabric.console.service;

import org.hyperledger.fabric.sdk.ChainConfiguration;
import org.hyperledger.fabric.sdk.EventHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import oxchains.fabric.console.auth.JwtAuthentication;
import oxchains.fabric.console.data.PeerRepo;
import oxchains.fabric.console.domain.*;
import oxchains.fabric.sdk.FabricSDK;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;
import static oxchains.fabric.sdk.domain.CAUser.fromUser;

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
        return fabricSDK
          .chainPeers(chainname)
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
          .stream() .map(EventHubInfo::new)
          .collect(toList());
    }

    private Optional<User> userContext() {
        return ((JwtAuthentication) getContext().getAuthentication()).user();
    }

    private boolean eventHubAttached(String id, String endpoint) {
        if (nonNull(endpoint)) {
            Optional<EventHub> eventHubOptional = fabricSDK.withEventHub(id, endpoint);
            if (eventHubOptional.isPresent()) {
                return fabricSDK.attachEventHubToChain(eventHubOptional.get());
            }
        }
        return true;
    }

    public boolean newChain(String chain, MultipartFile config) {
        try {
            ChainConfiguration chainConfiguration = new ChainConfiguration(config.getBytes());
            return userContext().flatMap(context ->
                fabricSDK.withUserContext(fromUser(context)).constructChain(chain, chainConfiguration)
            ).isPresent();
        }catch (Exception e){
            LOG.error("failed to construct chain {}", e.getMessage());
        }
        return false;

    }

    public boolean joinChain(String chainname, String peerId) {
        return peerRepo.findPeerEventhubById(peerId).flatMap(peerEventhub ->
            userContext().flatMap(context ->
                fabricSDK.withPeer(peerEventhub.getId(), peerEventhub.getEndpoint()).map(peer ->
                    fabricSDK.withUserContext(fromUser(context)).joinChain(peer, chainname)
                )
            )
        ).orElse(false);
    }
}
