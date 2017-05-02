package oxchains.fabric.sdk.stories;

import net.thucydides.core.annotations.Steps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import oxchains.fabric.sdk.steps.UserManagementSteps;

/**
 * @author aiet
 */
public class UserManagementStory {

    @Steps private UserManagementSteps userManagementSteps;

    @Given("fabric ca server $caServerEndpoint")
    public void givenFabricCAServer(String caServerEndpoint) throws Exception {
        userManagementSteps.fabricCAServerAt(caServerEndpoint);
    }

    @Given("manager account $username : $password")
    public void givenManagerAccount(String username, String password) {
        userManagementSteps.managerAccountIs(username, password);
    }

    @When("I enroll admin with wrong account $username")
    public void whenIEnrollAdminWithWrongAccount(String username){
        userManagementSteps.enrollAdminWithUsername(username);
    }

    @Then("admin enrollment fail")
    public void thenAdminEnrollmentFail(){
        userManagementSteps.adminEnrollmentShouldFail();
    }

    @When("I enroll admin with wrong password $password")
    public void whenIEnrollAdminWithWrongPassword(String password){
        userManagementSteps.enrollAdminWithPassword(password);
    }

    @When("I register a new user $username : $password")
    public void whenIRegisterANewUser(String username, String password) throws Exception {
        userManagementSteps.registerUser(username, password);
    }

    @Then("register failed")
    public void thenRegisterFailed() {
        userManagementSteps.registrationShouldFail();
    }

    @When("I enroll admin")
    public void whenIEnrollAdmin() throws Exception {
        userManagementSteps.enrollAdmin();
    }

    @When("I enroll user")
    public void whenIEnrollUser() throws Exception {
        userManagementSteps.enrollUser();
    }

    @Then("admin enrollment succeed")
    public void thenAdminEnrollmentSucceed() {
        userManagementSteps.adminEnrollmentShouldSucceed();
    }

    @Then("user enrollment succeed")
    public void thenUserEnrollmentSucceed() {
        userManagementSteps.userEnrollmentShouldSucceed();
    }

    @Then("registration succeed")
    public void thenRegistrationSucceed() {
        userManagementSteps.registrationShouldSucceed();
    }

    @Given("registered user $username : $password")
    public void givenRegisteredUser(String username, String password) {
        userManagementSteps.userRegistered(username, password);
    }

    @When("I revoke a user $username")
    public void whenIRevokeAUser(String username) {
        userManagementSteps.revokeUser(username);
    }

    @Then("revocation failed")
    public void thenRevocationFailed() {
        userManagementSteps.revocationShouldFail();
    }

    @Then("revocation succeed")
    public void thenRevocationSucceed() {
        userManagementSteps.revocationShouldSucceed();
    }

    @Then("user enrollment failed")
    public void thenEnrollmentFail(){
        userManagementSteps.userEnrollmentShouldFail();
    }
}
