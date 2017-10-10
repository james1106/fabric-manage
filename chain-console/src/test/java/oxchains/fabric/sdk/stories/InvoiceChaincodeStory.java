package oxchains.fabric.sdk.stories;

import net.thucydides.core.annotations.Steps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import oxchains.fabric.console.domain.ChainCodeInfo;
import oxchains.fabric.sdk.steps.ChaincodeAPISteps;

import static oxchains.fabric.util.StoryTestUtil.scriptParse;

/**
 * @author aiet
 */
public class InvoiceChaincodeStory {

    @Steps ChaincodeAPISteps chaincodeSteps;


    @Given("invoice chain $chainName")
    public void givenInvoiceChain(String chainName){
        chaincodeSteps.checkChain(chainName);
    }

    @Given("invoice chaincode $chaincode")
    public void givenInvoiceChaincode(String chaincode){
        chaincodeSteps.checkChaincode(chaincode);
    }

    @Given("listens event-hub $eventName at $eventEndpoint")
    public void givenEventhub(String eventName, String eventEndpoint) throws Exception {
        chaincodeSteps.chainWithEventHub(eventName, eventEndpoint);
    }

    @When("I instantiate invoice chaincode $chaincodeName with $arg")
    public void whenInstantiateChaincodeWith(String chaincodeName, String arg) throws Exception {
        chaincodeSteps.instantiateWith(chaincodeName, arg);
    }

    @Then("invoice intantiation succeed")
    public void thenIntantiationSucceed() {
        chaincodeSteps.instantiationSucceeded();
    }

    @When("I create invoice with: $args")
    public void whenICreateInvoiceWith(ChainCodeInfo chainCodeInfo, String args) throws Exception {
        chaincodeSteps.invokeChainWith(chainCodeInfo,scriptParse(args));
    }

    @Then("creation success")
    public void thenCreateSucceeded() {
        chaincodeSteps.invokeSucceeded();
    }

    @When("I $action invoice: $args")
    public void whenIQueryInvoiceWith(ChainCodeInfo chainCodeInfo, String action, String args) throws Exception {
        switch (action) {
        case "query":
            chaincodeSteps.queryChainWith(chainCodeInfo, args);
            break;
        case "transfer":
            chaincodeSteps.invokeChainWith(chainCodeInfo,scriptParse(args));
            break;
        default:
            break;
        }
    }

    @When("I request a reimbursement with $args")
    public void whenICreateReimbursementWith(ChainCodeInfo chainCodeInfo, String args) throws Exception {
        chaincodeSteps.invokeChainWith(chainCodeInfo, scriptParse(args));
    }

    @Then("reimbursement request accepted")
    public void thenRequestAccepted() {
        chaincodeSteps.invokeSucceeded();
    }

    @When("I reject reimbursement with $arg")
    public void whenIRejectReimbursementWith(ChainCodeInfo chainCodeInfo, String arg) throws Exception {
        chaincodeSteps.invokeChainWith(chainCodeInfo, scriptParse(arg));
    }

    @When("I query reimbursement with $arg")
    public void whenIQueryReimbursementWith(ChainCodeInfo chainCodeInfo, String arg) {
        chaincodeSteps.queryChainWith(chainCodeInfo, arg);
    }

    @When("I confirm reimbursement with $arg")
    public void whenIConfirmReimbursementWith(ChainCodeInfo chainCodeInfo, String arg) throws Exception {
        chaincodeSteps.invokeChainWith(chainCodeInfo, scriptParse(arg));
    }

    @Then("reimbursement $bxid has been $action")
    public void thenReimbursementActionDone(String bxid, String action) {
        chaincodeSteps.invokeSucceeded();
    }

    @Then("invoice should not contain $invoiceId")
    public void thenShouldNotContain(String invoiceId) {
        chaincodeSteps.resultContainsNo(invoiceId);
    }

    @Then("invoice should contain $invoiceId")
    public void thenShouldContain(String invoiceId) {
        chaincodeSteps.resultContains(invoiceId);
    }

    @Then("invoice transfer succeed")
    public void thenTransferSucceed() {
        chaincodeSteps.invokeSucceeded();
    }

    @Then("invoice transfer fail")
    public void thenTransferFail(){
        chaincodeSteps.invokeFailed();
    }

    @Then("query fail")
    public void thenQueryFail(){
        chaincodeSteps.queryFail();
    }

}
