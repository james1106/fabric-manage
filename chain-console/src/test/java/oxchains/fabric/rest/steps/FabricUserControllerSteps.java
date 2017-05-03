package oxchains.fabric.rest.steps;

import io.restassured.module.mockmvc.response.MockMvcResponse;
import net.thucydides.core.annotations.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.console.data.UserRepo;
import oxchains.fabric.console.domain.User;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class FabricUserControllerSteps {

    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;
    private final String defaultAffilicationKey = "#affiliation";

    private MockMvcResponse mockMvcResponse;

    @Step("list current users")
    public void listUsers() {
        mockMvcResponse = given()
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
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.token", notNullValue());
    }

    @Step("enroll with {0}:{1}")
    public void enrollWith(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        mockMvcResponse = given()
          .contentType(JSON)
          .body(user)
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
    public void registerUserWith(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setAffiliation(propertyParse(defaultAffilicationKey, testProperties));
        mockMvcResponse = given()
          .contentType(JSON)
          .body(user)
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

    @Step("with admin {0}:{1}")
    public void withAdmin(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setId(1L);
        userRepo.save(user);
    }

    @Step("should not include {0}")
    public void shouldNotInclude(String username) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.username", everyItem(not(username)));
    }
}
