package oxchains.fabric.rest.steps;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import net.thucydides.core.annotations.Step;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.console.data.UserRepo;
import oxchains.fabric.console.domain.User;
import oxchains.fabric.sdk.domain.CAUser;

import java.util.Map;
import java.util.Properties;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hyperledger.fabric.sdk.security.CryptoSuite.Factory.getCryptoSuite;
import static org.junit.Assert.assertNotNull;
import static oxchains.fabric.console.auth.Authorities.MANAGE;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class FabricUserControllerSteps {

    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;

    private MockMvcResponse mockMvcResponse;
    private User givenAccount;
    private CAUser admin;
    private String authentication;
    private HFCAClient hfcaClient;

    @Step("list current users")
    public void listUsers() {
        mockMvcResponse = given()
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + authentication)
          .when()
          .get("/user");
    }

    @Step("there should be a user with name: {0}")
    public void usersContain(String username) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.username", hasItem(username));
    }

    @Step("then token should be in the response")
    public void tokenReturned() {
        authentication = mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.token", notNullValue())
          .body("data.user.certificate", notNullValue())
          .and()
          .extract()
          .path("data.token");
    }

    @Step("enroll with {0}:{1} of org {2}, {3}@{4}")
    public void enrollWith(String username, String password, String org, String ca, String uri, String msp) throws Exception {
        User user = new User(username, org);
        user.setPassword(password);
        user.setCa(ca);
        user.setUri(propertyParse(uri, testProperties));
        user.setMsp(msp);
        ObjectMapper mapper = new ObjectMapper().disable(MapperFeature.USE_ANNOTATIONS);
        mockMvcResponse = given()
          .contentType(JSON)
          .body(mapper.writeValueAsString(user))
          .when()
          .post("/user/token");
    }

    @Step("enroll failed")
    public void enrollFailed() {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("status", is(-1));
    }

    @Step("registering user {0}:{1}")
    public void registerUserWith(String username, String password) throws Exception {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        ObjectMapper mapper = new ObjectMapper().disable(MapperFeature.USE_ANNOTATIONS);
        mockMvcResponse = given()
          .contentType(JSON)
          .body(mapper.writeValueAsString(user))
          .when()
          .post("/user");
    }

    @Step("registering user {0}:{1} with authentication")
    public void registerUserWithAuthAnd(String username, String password) throws Exception {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        ObjectMapper mapper = new ObjectMapper().disable(MapperFeature.USE_ANNOTATIONS);
        mockMvcResponse = given()
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + authentication)
          .contentType(JSON)
          .body(mapper.writeValueAsString(user))
          .when()
          .post("/user");
    }

    @Step("register {0} succeeded")
    public void userReturned(String username) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("status", is(1))
          .and()
          .body("data.username", is(username));
    }

    @Step("revoking {0}")
    public void revokeUser(String username) {
        mockMvcResponse = given()
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + authentication)
          .param("action", 0)
          .param("reason", 0)
          .when()
          .put("/user/" + username);
    }

    @Step("revocation succeeded")
    public void revocationSucceed() {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("status", is(1));
    }

    @Step("nothing found")
    public void nothingThere() {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data", empty());
    }

    @Autowired private UserRepo userRepo;

    @Step("with admin {0}:{1} from {2} registered at {3}@{4}")
    public void withAdmin(String username, String password, String org, String caName, String caUri, String msp) throws Exception {
        User user = new User(username, org);
        user.setPassword(password);
        user.setCa(caName);
        user.setUri(propertyParse(caUri, testProperties));
        user.setMsp(msp);
        user.setAuthorities(Sets.newHashSet(MANAGE));

        RegistrationRequest registrationRequest = new RegistrationRequest(username, org);
        //registrationRequest.setCAName(caName);
        registrationRequest.addAttribute(new Attribute("hf.Registrar.Roles", "user"));
        registrationRequest.addAttribute(new Attribute("hf.Revoker", "true"));
        registrationRequest.setSecret(password);
        String registrationResult = hfcaClient.register(registrationRequest, admin);
        assertNotNull("registration should have succeeded", registrationResult);

        givenAccount = userRepo.save(user);
    }

    @Step("should not include {0}")
    public void shouldNotInclude(String username) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.username", everyItem(not(username)));
    }

    public void enrollWithGivenAccount() throws Exception {
        ObjectMapper mapper = new ObjectMapper().disable(MapperFeature.USE_ANNOTATIONS);
        mockMvcResponse = given()
          .contentType(JSON)
          .body(mapper.writeValueAsString(givenAccount))
          .when()
          .post("/user/token");
    }

    public void requestFailed() {
        mockMvcResponse
          .then()
          .statusCode(SC_FORBIDDEN)
          .and()
          .body("status", is(-1));
    }

    public void givenManager(String username, String password, String caName, String caUri) throws Exception{
        admin = new CAUser(propertyParse(username, testProperties), "", "");
        admin.setPassword(propertyParse(password, testProperties));
        hfcaClient = HFCAClient.createNewInstance(caName, propertyParse(caUri, testProperties), new Properties());
        hfcaClient.setCryptoSuite(getCryptoSuite());
        Enrollment adminEnrollment = hfcaClient.enroll(admin.getName(), admin.getPassword());
        admin.setEnrollment(adminEnrollment);
    }
}
