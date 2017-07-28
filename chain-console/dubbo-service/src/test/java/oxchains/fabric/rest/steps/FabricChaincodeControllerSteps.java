package oxchains.fabric.rest.steps;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import net.thucydides.core.annotations.Step;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.AfterStory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.console.data.UserRepo;
import oxchains.fabric.console.domain.User;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class FabricChaincodeControllerSteps {

    private Logger LOG = LoggerFactory.getLogger(getClass());
    private MockMvcResponse mockMvcResponse;

    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;
    @Value("${fabric.chaincode.path}") private String path;

    @AfterStory
    public void clean() {
        try {
            FileUtils.forceDelete(new File(path));
        } catch (Exception ignored) {
        }
    }

    @Step("uploading {0}")
    public void uploadChaincode(String chaincode) {
        byte[] bytes = new byte[1024];
        new Random().nextBytes(bytes);
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .contentType(MULTIPART_FORM_DATA_VALUE)
          .formParam("name", chaincode)
          .formParam("version", "1.0")
          .multiPart("chaincode", new File(String.format("src/test/resources/chaincode/upload/%s_chaincode.go", chaincode)))
          .when()
          .post("/chaincode/file");
    }

    @Step("chaincode uploaded")
    public void uploaded() {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("status", is(1));
    }

    @Step("installing chaincode {0} on peer {1}")
    public void installChaincodeOn(String chaincode, String chain, String peerId) {
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .queryParam("chain", chain)
          .queryParam("chaincode", chaincode)
          .queryParam("peers", peerId)
          .queryParam("version", "1.0")
          .queryParam("lang", "go")
          .when()
          .post("/chaincode/install");
    }

    @Step("chaincode {0} installed on peer {1}")
    public void installed(String chaincode, String peerId) {
        Matcher[] matchers = new Matcher[] { hasKey("txid"), hasEntry(peerId, 1) };
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data", hasItem(allOf(matchers)));
    }

    @Step("instantiating chaincode {0} with {1}")
    public void instantiateWith(String chaincode, String chain, String args) {

        File endorsementPolicy = new File("src/test/resources/chain_configuration/" + chaincode + "_endorsement_policy.yaml");

        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .queryParam("chain", chain)
          .queryParam("name", chaincode)
          .queryParam("version", "1.0")
          .queryParam("args", join(args.split(" "), ","))
          .multiPart("endorsement", endorsementPolicy)
          .when()
          .post("/chaincode");
    }

    @Step("chaincode {0} instantiated")
    public void instantiated(String chaincode) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .body("data.txid", notNullValue());
    }

    @Step("committing a transaction on {0} of {1} with {2}")
    public void triggerTransaction(String chaincode, String chain, String args) {
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .queryParam("version", "1.0")
          .queryParam("chaincode", chaincode)
          .queryParam("chain", chain)
          .queryParam("args", join(args.split(" "), ","))
          .when()
          .post("/chaincode/tx");
    }

    @Step("transaction done on {0}")
    public void transactionDone(String chaincode) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .body("data.txid", notNullValue());
    }

    @Step("fetching all chaincodes")
    public void chaincodes() {
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .when()
          .get("/chaincode");
    }

    @Step("all chaincodes include {0}")
    public void chaincodeIncludes(String chaincode) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.name", hasItem(chaincode));
    }

    @Step("chaincode {0} is marked installed")
    public void chaincodeMarkedInstalled(String chaincode) {
        Matcher[] matchers = new Matcher[] { hasEntry("name", chaincode), hasEntry(is("installed"), not(empty())) };
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data", hasItem(allOf(matchers)));
    }

    @Step("query chaincode {0} with args {1}")
    public void queryWith(String chaincode, String chain, String args) {
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .queryParam("chaincode", chaincode)
          .queryParam("version", "1.0")
          .queryParam("chain", chain)
          .queryParam("args", join(args.split(" "), ","))
          .when()
          .get("/chaincode/tx");
    }

    @Step("query result of chaincode {0} is {1}:{2}")
    public void queryResultHas(String chaincode, String key, String value) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data." + key, is(value))
          .and()
          .body("data.txid", notNullValue());
    }

    @Step("chaincode {0} is marked instantiated")
    public void chaincodeMarkedInstantiated(String chaincode) {
        Matcher[] matchers = new Matcher[] { hasEntry("name", chaincode), hasEntry(is("instantiated"), not(empty())) };
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data", hasItem(allOf(matchers)));
    }

    @Autowired private UserRepo userRepo;
    private String token;

    @Step("with token of {0} from {1}")
    public void withTokenOf(String username, String affiliation) throws Exception {
        Optional<User> userOptional = userRepo.findUserByUsernameAndAffiliation(username, affiliation);
        assertTrue("given admin should have been registered", userOptional.isPresent());

        User givenAccount = userOptional.get();
        ObjectMapper mapper = new ObjectMapper().disable(MapperFeature.USE_ANNOTATIONS);
        this.token = given()
          .contentType(JSON)
          .body(mapper.writeValueAsString(givenAccount))
          .when()
          .post("/user/token")
          .then()
          .body("data.token", notNullValue())
          .and()
          .extract()
          .path("data.token");

        Optional<User> savedUserOptional = userRepo.findUserByUsernameAndAffiliation(username, affiliation);
        assertNotNull("given admin should have been enrolled", savedUserOptional
          .get()
          .getCertificate());
        LOG.info("admin certificate: \n{}\n", savedUserOptional
          .get()
          .getCertificate());
    }
}
