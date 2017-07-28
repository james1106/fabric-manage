package oxchains.fabric.sdk.stories;

import net.thucydides.core.annotations.Steps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import oxchains.fabric.sdk.steps.CASteps;
import oxchains.fabric.sdk.steps.ChainAPISteps;
import oxchains.fabric.sdk.steps.PeerAPISteps;

import java.io.IOException;

/**
 * @author aiet
 */
public class PeerJoinChainStory {

    @Steps ChainAPISteps chainAPISteps;
    @Steps PeerAPISteps peerAPISteps;
    @Steps CASteps caSteps;

    @Given("admin user context")
    public void givenAdminContext() throws Exception {
        caSteps.adminContext();
    }

    @Given("chain $chainName created at orderer $ordererEndpoint")
    public void givenChannelFooCreated(String chainName, String ordererEndpoint) throws IOException {
        chainAPISteps.buildOrdererFrom(ordererEndpoint);
        chainAPISteps.constructChainOf(chainName);
    }

    @Given("peer $peerId at $peerEndpoint")
    public void givenPeerAt(String peerId, String peerEndpoint) {
        peerAPISteps.peerCreated(peerId, peerEndpoint);
    }

    @When("$peerId joins chain $chainName")
    public void whenPeerJoinChain(String peerId, String chainName) {
        peerAPISteps.peerJoinChain(peerId, chainName);
    }

    @Then("$peerId has joined $chainName")
    public void thenPeerShouldJoin(String peerId, String chainName) {
        peerAPISteps.peerHasJoinedChain(peerId, chainName);
    }

    @Then("$peerId cannot join $chainName")
    public void thenPeerCannotJoin(String peerId, String chainName) {
        peerAPISteps.peerShoudNotJoinChain(peerId, chainName);
    }

    @When("query chain of $peer")
    public void whenQueryChainOfPeer(String peerId) {
        peerAPISteps.queryChainOfPeer(peerId);
    }

    @Then("chains include $chainName")
    public void thenChainsInclude(String chainName) {
        peerAPISteps.chainIncludes(chainName);
    }

}
