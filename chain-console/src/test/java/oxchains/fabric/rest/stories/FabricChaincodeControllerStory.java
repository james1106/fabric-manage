package oxchains.fabric.rest.stories;

import net.thucydides.core.annotations.Steps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import oxchains.fabric.rest.steps.FabricChaincodeControllerSteps;

/**
 * @author aiet
 */
public class FabricChaincodeControllerStory {

    @Steps FabricChaincodeControllerSteps steps;

    @Given("fabric console for chaincode")
    public void givenFabricConsoleForChaincode() {
    }


    @Given("added peer $peerId at $peerEndpoint with eventhub at $eventHub")
    public void givenPeerAndEventhub(String peerId, String peerEndpoint, String eventHub){
        steps.withPeerAndEventHub(peerId, peerEndpoint, eventHub);
    }

    @When("I upload chaincode $chaincode")
    public void whenIUploadChaincode(String chaincode) {
        steps.uploadChaincode(chaincode);
    }

    @Then("upload succeed")
    public void thenUploadSucceed() {
        steps.uploaded();
    }

    @When("I install chaincode $chaincode on $peerId")
    public void whenIInstallChaincodeOnPeer(String chaincode, String peerId) {
        steps.installChaincodeOn(chaincode, peerId);
    }

    @Then("chaincode $chaincode installed on $peerId")
    public void thenChaincodeInstalled(String chaincode, String peerId) {
        steps.installed(chaincode, peerId);
    }

    @When("I instantiate $chaincode chaincode with: $args")
    public void whenIInstantiateChaincodeWith(String chaincode, String args) {
        steps.instantiateWith(chaincode, args);
    }

    @Then("$chaincode chaincode intantiation succeed")
    public void thenChaincodeIntantiationSucceed(String chaincode) {
        steps.instantiated(chaincode);
    }

    @When("I trigger a transaction on $chaincode chaincode with: $args")
    public void whenITriggerTransactionOnChaincodeWith(String chaincode, String args) {
        steps.triggerTransaction(chaincode, args);
    }

    @Then("transaction on $chaincode chaincode succeed")
    public void thenTransactionOnChaincodeSucceed(String chaincode) {
        steps.transactionDone(chaincode);
    }

    @When("I check all chaincodes")
    public void whenGetChaincodes(){
        steps.chaincodes();
    }

    @Then("there is chaincode $chaincode")
    public void thenChaincodesIncludes(String chaincode){
        steps.chaincodeIncludes(chaincode);
    }

    @Then("chaincode $chaincode is marked installed")
    public void thenMarkedInstalledForChaincode(String chaincode){
        steps.chaincodeMarkedInstalled(chaincode);
    }

    @When("I make a query on $chaincode chaincode with: $args")
    public void whenQueryChaincodeWith(String chaincode, String args){
        steps.queryWith(chaincode, args);
    }

    @Then("result of $chaincode chaincode has $key of $value")
    public void thenQueryResultHaveKeyValue(String chaincode, String key, String value){
        steps.queryResultHas(chaincode, key, value);
    }

}
