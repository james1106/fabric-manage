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

    @Then("there is no peer $peerId yet")
    public void thenThereIsNoPeerYet(String peerId) {
        steps.noPeerYet(peerId);
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

    @When("I get current eventhubs")
    public void whenIGetCurrentEventHubs(){
        steps.getEventhubs();
    }
    @Then("eventhub $eventhubId should be found")
    public void thenEventhubFound(String eventhubId){
        steps.foundEventHub(eventhubId);
    }

    @Then("chain $chainName should be found")
    public void oneChainFound(String chainName){
        steps.foundChain(chainName);
    }

    @When("I stop $peerId")
    public void stopPeer(String peerId){
        steps.stopPeer(peerId);
    }

    @When("I start $peerId")
    public void startPeer(String peerId){
        steps.startPeer(peerId);
    }

    @Then("operation success")
    public void thenPeerOperationSuccess(){
        steps.operationDone();
    }

}
