package oxchains.fabric.sdk.steps;

import net.thucydides.core.annotations.Step;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.dummy.PlainTestApplication;
import oxchains.fabric.sdk.domain.CAUser;
import oxchains.fabric.sdk.domain.FabricUser;

import java.util.Map;
import java.util.Properties;

import static org.hyperledger.fabric.sdk.security.CryptoSuite.Factory.getCryptoSuite;
import static org.hyperledger.fabric_ca.sdk.RevokeReason.UNSPECIFIED;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */
@ContextConfiguration(classes = PlainTestApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class UserManagementSteps {

    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;

    private HFCAClient caClient;
    private CAUser admin;
    private FabricUser user;
    private final String defaultAffilicationKey = "#affiliation";
    private String registrationResult;
    private Exception registerException;
    private Exception adminException;
    private Exception userException;
    private Exception revocationException;

    @Step("connect to fabric server {0}")
    public void fabricCAServerAt(String caServerEndpoint) throws Exception {
        caClient = HFCAClient.createNewInstance(propertyParse(caServerEndpoint, testProperties), new Properties());
        caClient.setCryptoSuite(getCryptoSuite());
    }

    @Step("manager account {0}:{1}")
    public void managerAccountIs(String username, String password) {
        admin = new CAUser(propertyParse(username, testProperties), propertyParse(defaultAffilicationKey, testProperties), "");
        admin.setPassword(propertyParse(password, testProperties));
    }

    @Step("registering a user {0}:{1}")
    public void registerUser(String username, String password) {
        try {
            RegistrationRequest registrationRequest = new RegistrationRequest(username, propertyParse(defaultAffilicationKey, testProperties));
            registrationRequest.setSecret(password);
            user = new FabricUser(username, propertyParse(defaultAffilicationKey, testProperties));
            user.setPassword(password);
            registrationResult = caClient.register(registrationRequest, admin);
            registerException = null;
        }catch (Exception e){
            registerException = e;
        }
    }

    @Step("registration failure")
    public void registrationShouldFail() {
        assertNull("registration should have failed", registrationResult);
        assertNotNull("registration should have failed with exception", registerException);
    }

    @Step("enrolling admin")
    public void enrollAdmin() {
        try {
            Enrollment adminEnrollment = caClient.enroll(admin.getName(), admin.getPassword());
            admin.setEnrollment(adminEnrollment);
            adminException = null;
        } catch (Exception e) {
            adminException = e;
        }
    }

    @Step("enrolling user")
    public void enrollUser() {
        try {
            Enrollment userEnrollment = caClient.enroll(user.getName(), user.getPassword());
            user.setEnrollment(userEnrollment);
            userException = null;
        } catch (Exception e) {
            userException = e;
        }
    }

    @Step("admin enrollment success")
    public void adminEnrollmentShouldSucceed() {
        assertNotNull("enrollment should have succeeded", admin.getEnrollment());
        assertNull("enrollment should have succeeded without any exception", adminException);
    }

    @Step("user enrollment success")
    public void userEnrollmentShouldSucceed() {
        assertNotNull("enrollment should have succeeded", user.getEnrollment());
        assertNull("enrollment should have succeeded without any exception", userException);
    }

    @Step("registration success")
    public void registrationShouldSucceed() {
        assertNotNull("registration should have succeeded", registrationResult);
        assertNull("registration should have succeeded without any exception", registerException);
    }

    @Step("user {0}:{1} is already registered")
    public void userRegistered(String username, String password) {
        user = new FabricUser(username, propertyParse(defaultAffilicationKey, testProperties));
        user.setPassword(password);
    }

    @Step("user {0} is being revoked")
    public void revokeUser(String username) {
        try {
            caClient.revoke(admin, username, UNSPECIFIED);
            revocationException = null;
        } catch (Exception e) {
            revocationException = e;
        }
    }

    @Step("revocation failure")
    public void revocationShouldFail() {
        assertNotNull("revocation should have failed", revocationException);
    }

    @Step("revocation success")
    public void revocationShouldSucceed() {
        assertNull("revocation should have failed", revocationException);
    }

    @Step("enrolling admin {0}")
    public void enrollAdminWithUsername(String username) {
        try {
            Enrollment adminEnrollment = caClient.enroll(username, admin.getPassword());
            admin.setEnrollment(adminEnrollment);
            adminException = null;
        } catch (Exception e) {
            adminException = e;
        }
    }

    @Step("enrolling admin with pass {0}")
    public void enrollAdminWithPassword(String password) {
        try {
            Enrollment adminEnrollment = caClient.enroll(admin.getName(), password);
            admin.setEnrollment(adminEnrollment);
            adminException = null;
        } catch (Exception e) {
            adminException = e;
        }
    }

    @Step("admin enrollment failed")
    public void adminEnrollmentShouldFail() {
        assertNull("admin enrollment should have failed", admin.getEnrollment());
        assertNotNull("admin enrollment should have failed with exception", adminException);
    }

    @Step("user enrollment failed")
    public void userEnrollmentShouldFail() {
        assertNull("user enrollment should have failed", user.getEnrollment());
        assertNotNull("user enrollment should have failed", userException);
    }
}
