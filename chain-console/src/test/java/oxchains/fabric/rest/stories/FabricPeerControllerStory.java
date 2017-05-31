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

    @Given("peer admin $username of org $affiliation enrolled")
    public void enrollWithGivenAccount(String username, String affiliation) throws Exception {
        steps.enrolledAdmin(username, affiliation);
    }

    @When("I get current peers")
    public void whenIGetCurrentPeers() {
        steps.getPeers();
    }

    @Then("there is no peer $peerId yet")
    public void thenThereIsNoPeerYet(String peerId) {
        steps.noPeerYet(peerId);
    }

    @When("I add peer $peerId at $peerEndpoint with eventhub at $eventHubEndpoint, password $pass")
    public void whenIAddPeer(String peerId, String peerEndpoint, String eventHubEndpoint, String pass) throws Exception {
        steps.addPeerWithEventhub(peerId, peerEndpoint, eventHubEndpoint, pass);
    }

    @Then("new peer added")
    public void thenPeerAdded() {
        steps.operationDone();
    }

    @Then("peer $peerId should be found")
    public void onePeerFound(String peerId) {
        steps.foundPeer(peerId);
    }

    @Then("peer $peerId not connected")
    public void notConnectedToPeer(String peerId){
        steps.peerNotConnected(peerId);
    }

    @Then("chain $chainName should be found")
    public void oneChainFound(String chainName){
        steps.foundChain(chainName);
    }

    @When("connect to peer $peerId")
    public void whenConnectToPeer(String peerId) throws Exception {
        steps.connectPeer(peerId);
    }

    @Then("peer $peerId connected")
    public void thenConnectedPeer(String peerId){
        steps.operationDone();
    }

    @Then("peer $peerId should be connected")
    public void thenPeerConnectedInList(String peerId){
        steps.peerInListConnected(peerId);
    }

    @When("I enroll peer $peerId : $pass from org $org, CA $ca at $caUri with msp $msp")
    public void whenEnrollPeer(String peerId, String pass, String org, String ca, String caUri, String msp) throws Exception {
        steps.enrollPeer(peerId, pass, org, ca, caUri, msp);
    }

    @Then("peer $peerId enrolled")
    public void thenEnrolledPeer(String peerId){
        steps.enrolledPeer(peerId);
    }

}
