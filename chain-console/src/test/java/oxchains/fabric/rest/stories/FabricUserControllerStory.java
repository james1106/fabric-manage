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

    @Given("fabric console for user")
    public void givenFabricConsole() {
    }

    @Given("$username : $password as admin")
    public void givenAdmin(String username, String password){
        steps.withAdmin(username, password);
    }

    @When("I get current users")
    public void whenIGetCurrentUsers() {
        steps.listUsers();
    }

    @Then("$username should be included")
    public void thenShouldBeInclude(String username) {
        steps.usersContain(username);
    }

    @When("I enroll with $username : $password")
    public void whenEnrollWith(String username, String password) {
        steps.enrollWith(username, password);
    }

    @Then("token should be in the response")
    public void thenTokenReturned() {
        steps.tokenReturned();
    }

    @Then("enrollment failed")
    public void thenEnrollFailed() {
        steps.enrollFailed();
    }

    @When("I register user $username : $password")
    public void whenIRegisterUser(String username, String password) {
        steps.registerUserWith(username, password);
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
