package oxchains.fabric.rest.stories;

import net.thucydides.core.annotations.Steps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import oxchains.fabric.rest.steps.FabricPeerControllerSteps;

/**
 * @author aiet
 */
public class FabricPeerControllerStory {

    @Steps FabricPeerControllerSteps steps;

    @Given("fabric console for peer")
    public void givenFabricConsole(){}

    @When("I get current peers")
    public void whenIGetCurrentPeers() {
        steps.getPeers();
    }

    @Then("there is no peer yet")
    public void thenThereIsNoPeerYet() {
        steps.noPeerYet();
    }

    @When("I add peer $peerId at $peerEndpoint with eventhub at $eventHubEndpoint")
    public void whenIAddPeerAt(String peerId, String peerEndpoint, String eventHubEndpoint) {
        steps.addPeerWithEventhub(peerId, peerEndpoint, eventHubEndpoint);
    }

    @Then("new peer added")
    public void thenPeerAdded() {
        steps.newPeerAdded();
    }

    @Then("peer $peerId should be found")
    public void onePeerFound(String peerId) {
        steps.foundPeer(peerId);
    }


    @Then("chain $chainName should be found")
    public void oneChainFound(String chainName){
        steps.foundChain(chainName);
    }

}
