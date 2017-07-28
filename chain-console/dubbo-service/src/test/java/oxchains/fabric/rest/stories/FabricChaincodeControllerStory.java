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

    @Given("token of chain admin $username from org $affiliation")
    public void givenAdmin(String username, String affiliation) throws Exception {
        steps.withTokenOf(username, affiliation);
    }

    @When("I upload chaincode $chaincode")
    public void whenIUploadChaincode(String chaincode) {
        steps.uploadChaincode(chaincode);
    }

    @Then("upload succeed")
    public void thenUploadSucceed() {
        steps.uploaded();
    }

    @When("I install chaincode $chaincode on $peerId of chain $chain")
    public void whenIInstallChaincodeOnPeer(String chaincode, String peerId, String chain) {
        steps.installChaincodeOn(chaincode, chain, peerId);
    }

    @Then("chaincode $chaincode installed on $peerId")
    public void thenChaincodeInstalled(String chaincode, String peerId) {
        steps.installed(chaincode, peerId);
    }

    @When("I instantiate $chaincode chaincode of chain $chain with: $args")
    public void whenIInstantiateChaincodeWith(String chaincode, String chain, String args) {
        steps.instantiateWith(chaincode, chain, args);
    }

    @Then("$chaincode chaincode intantiation succeed")
    public void thenChaincodeIntantiationSucceed(String chaincode) {
        steps.instantiated(chaincode);
    }

    @When("I trigger a transaction on $chaincode chaincode of chain $chain with: $args")
    public void whenITriggerTransactionOnChaincodeWith(String chaincode, String chain, String args) {
        steps.triggerTransaction(chaincode, chain, args);
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

    @Then("chaincode $chaincode is marked instantiated")
    public void thenChaincodeInstantiated(String chaincode){
        steps.chaincodeMarkedInstantiated(chaincode);
    }

    @When("I make a query on $chaincode chaincode of chain $chain with: $args")
    public void whenQueryChaincodeWith(String chaincode, String chain, String args){
        steps.queryWith(chaincode, chain, args);
    }

    @Then("result of $chaincode chaincode has $key of $value")
    public void thenQueryResultHaveKeyValue(String chaincode, String key, String value){
        steps.queryResultHas(chaincode, key, value);
    }

}
