package oxchains.fabric.rest.stories;

import net.thucydides.core.annotations.Steps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import oxchains.fabric.rest.steps.FabricUserControllerSteps;

/**
 * @author aiet
 */
public class FabricUserControllerStory {

    @Steps FabricUserControllerSteps steps;

    @Given("manager account $username : $password on CA $caName at $caUri")
    public void givenManager(String username, String password, String caName, String caUri) throws Exception {
        steps.givenManager(username, password, caName, caUri);
    }

    @Given("$username : $password as admin of org $org, CA $caName at $caUri with msp $msp")
    public void givenAdmin(String username, String password, String org, String caName, String caUri, String msp) throws Exception {
        steps.withAdmin(username, password, org, caName, caUri, msp);
    }

    @When("I enroll with the given account")
    public void enrollWithGivenAccount() throws Exception {
        steps.enrollWithGivenAccount();
    }

    @When("I get current users")
    public void whenIGetCurrentUsers() {
        steps.listUsers();
    }

    @Then("request rejected")
    public void thenRejected(){
        steps.requestFailed();
    }

    @Then("$username should be included")
    public void thenShouldBeInclude(String username) {
        steps.usersContain(username);
    }

    @When("I enroll with $username : $password of org $org, CA $ca at $caUri with msp $msp")
    public void whenEnrollWith(String username, String password, String org, String ca, String caUri, String msp) throws Exception {
        steps.enrollWith(username, password, org, ca, caUri, msp);
    }

    @Then("token should be in the response")
    public void thenTokenReturned() {
        steps.tokenReturned();
    }

    @Then("enrollment failed")
    public void thenEnrollFailed() {
        steps.enrollFailed();
    }

    @When("I register user $username : $password without authentication")
    public void whenIRegisterUserWithoutAuth(String username, String password) throws Exception {
        steps.registerUserWith(username, password);
    }

    @When("I register user $username : $password with authentication")
    public void whenIRegisterUserWithAuth(String username, String password) throws Exception {
        steps.registerUserWithAuthAnd(username, password);
    }

    @Then("registration failed")
    public void thenRegisterFail(){
        steps.requestFailed();
    }

    @Then("$username should be in the response")
    public void thenUserReturned(String username) {
        steps.userReturned(username);
    }

    @When("I revoke user $username")
    public void whenIRevokeUserDemo(String username) {
        steps.revokeUser(username);
    }

    @Then("revocation should succeed")
    public void thenRevocationShouldSucceed() {
        steps.revocationSucceed();
    }

    @Then("$username should not be included")
    public void thenShouldNotInclude(String username){
        steps.shouldNotInclude(username);
    }

    @Then("there is no user yet")
    public void thenThereIsNothing(){
        steps.nothingThere();
    }

}
