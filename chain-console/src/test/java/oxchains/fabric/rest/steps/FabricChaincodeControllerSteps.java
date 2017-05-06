package oxchains.fabric.rest.steps;

import io.restassured.module.mockmvc.response.MockMvcResponse;
import net.thucydides.core.annotations.Step;
import org.apache.commons.io.FileUtils;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.AfterStory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.console.domain.PeerEventhub;

import java.io.File;
import java.util.Map;
import java.util.Random;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class FabricChaincodeControllerSteps {

    MockMvcResponse mockMvcResponse;

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
    public void installChaincodeOn(String chaincode, String peerId) {
        mockMvcResponse = given()
          .when()
          .post(String.format("/chaincode/install/%s?peers=%s", chaincode, peerId));
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
    public void instantiateWith(String chaincode, String args) {

        File endorsementPolicy = new File("src/test/resources/chain_configuration/" + chaincode + "_endorsement_policy.yaml");

        mockMvcResponse = given()
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

    @Step("committing a transaction on {0} with {1}")
    public void triggerTransaction(String chaincode, String args) {
        mockMvcResponse = given()
          .queryParam("args", join(args.split(" "), ","))
          .when()
          .post(String.format("/chaincode/tx/%s/" + "1.0", chaincode));
    }

    @Step("transaction done on {0}")
    public void transactionDone(String chaincode) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .body("data.txid", notNullValue());
    }

    @Step("connecting peer {0}@{1} and eventhub {2}")
    public void withPeerAndEventHub(String peerId, String peerEndpoint, String eventHub) {
        PeerEventhub peerInfo = new PeerEventhub();
        peerInfo.setId(peerId);
        peerInfo.setEndpoint(propertyParse(peerEndpoint, testProperties));
        peerInfo.setEventhub(propertyParse(eventHub, testProperties));
        mockMvcResponse = given()
          .contentType(JSON.withCharset(UTF_8))
          .body(peerInfo)
          .when()
          .post("/peer");

        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("status", is(1));
    }

    @Step("fetching all chaincodes")
    public void chaincodes() {
        mockMvcResponse = given()
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
        Matcher[] matchers = new Matcher[] { hasEntry("name", chaincode), hasEntry("installed", 1) };
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data", hasItem(allOf(matchers)));
    }

    @Step("query chaincode {0} with args {1}")
    public void queryWith(String chaincode, String args) {
        mockMvcResponse = given()
          .queryParam("args", join(args.split(" "), ","))
          .when()
          .get("/chaincode/tx/" + chaincode + "/1.0");
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
}
