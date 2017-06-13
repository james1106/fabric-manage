package oxchains.fabric.sdk.steps;

import net.thucydides.core.annotations.Step;
import org.hyperledger.fabric.sdk.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.sdk.FabricSDK;

import java.util.*;

import static org.junit.Assert.*;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class PeerAPISteps {

    @Autowired FabricSDK fabricSDK;
    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;
    private Set<String> chains = new HashSet<>(2);

    @Step("with peer {0} at {1}")
    public void peerCreated(String peerId, String peerEndpoint) {
        Optional<Peer> peerOptional = fabricSDK.withPeer(peerId, propertyParse(peerEndpoint, testProperties));
        assertTrue(peerOptional.isPresent());
    }

    @Step("peer {0} joins chain {1}")
    public void peerJoinChain(String peerId, String chainName) {
        Optional<Peer> peerOptional = fabricSDK.getPeer(peerId);
        assertTrue(peerOptional.isPresent());
        fabricSDK.joinChain(peerOptional.get(), chainName);
    }

    @Step("peer {0} joined chain {1}")
    public void peerHasJoinedChain(String peerId, String chainName) {
        assertNotNull("peer id needed", peerId);
        assertNotNull("chain needed", chainName);
        List<Peer> peers = fabricSDK.chainPeers(chainName);
        Optional<Peer> peerOptional = peers
          .stream()
          .filter(p -> peerId.equals(p.getName()))
          .findFirst();
        assertTrue(peerOptional.isPresent());
    }

    @Step("peer {0} did not join chain {1}")
    public void peerShoudNotJoinChain(String peerId, String chainName) {
        assertNotNull("chain needed", chainName);
        List<Peer> peers = fabricSDK.chainPeers(chainName);
        Optional<Peer> peerOptional = peers
          .stream()
          .filter(p -> peerId.equals(p.getName()))
          .findFirst();
        assertFalse(peerOptional.isPresent());
    }

    @Step("querying chains that {0} is in")
    public void queryChainOfPeer(String peerId) {
        Optional<Peer> peerOptional = fabricSDK.getPeer(peerId);
        assertTrue("peer should have been created", peerOptional.isPresent());
        chains.addAll(fabricSDK.chainsOfPeer(peerOptional.get()));
    }

    @Step("peer is in chain {0}")
    public void chainIncludes(String chainName) {
        assertTrue("peer should be in chain " + chainName, chains.contains(chainName));
    }
}
