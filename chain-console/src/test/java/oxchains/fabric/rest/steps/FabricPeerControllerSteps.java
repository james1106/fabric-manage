package oxchains.fabric.rest.steps;

import io.restassured.module.mockmvc.response.MockMvcResponse;
import net.thucydides.core.annotations.Step;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.console.domain.PeerEventhub;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class FabricPeerControllerSteps {

    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;
    private MockMvcResponse mockMvcResponse;

    @Step("fetching peers for default chain")
    public void getPeers() {
        mockMvcResponse = given()
          .when()
          .get("/peer");
    }

    @Step("no peer {0} on default chain")
    public void noPeerYet(String peerId) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.id", not(hasItem(peerId)));
    }

    @Step("adding peer {0}:{1} and eventhub {2}")
    public void addPeerWithEventhub(String peerId, String peerEndpoint, String eventHubEndpoint) {
        PeerEventhub peerInfo = new PeerEventhub();
        peerInfo.setId(peerId);
        peerInfo.setEndpoint(propertyParse(peerEndpoint, testProperties));
        peerInfo.setEventhub(propertyParse(eventHubEndpoint, testProperties));
        mockMvcResponse = given()
          .contentType(JSON.withCharset(UTF_8))
          .body(peerInfo)
          .when()
          .post("/peer");
    }

    @Step("new peer added")
    public void newPeerAdded() {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("status", is(1));
    }

    @Step("peer {0} found")
    public void foundPeer(String peerId) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.id", hasItem(peerId));
    }

    @Step("chain {0} found")
    public void foundChain(String chainName) {
        mockMvcResponse
          .then()
          .body("data.chains", hasItems(hasItem(chainName)));
    }

    @Step("stopping peer {0}")
    public void stopPeer(String peerId) {
        mockMvcResponse = given()
          .queryParam("action", 0)
          .when()
          .put(String.format("/peer/%s/status", peerId));
    }

    @Step("starting peer {0}")
    public void startPeer(String peerId) {
        mockMvcResponse = given()
          .queryParam("action", 1)
          .when()
          .put(String.format("/peer/%s/status", peerId));
        try {
            TimeUnit.SECONDS.sleep(5);
        }catch (Exception ignored){}
    }

    @Step("peer operation succeeded")
    public void operationDone() {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("status", is(1));
    }

    @Step("fetching eventhubs of default chain")
    public void getEventhubs() {
        mockMvcResponse = given()
          .when()
          .get("/peer/eventhub");
    }

    @Step("eventhub {0} found")
    public void foundEventHub(String eventhubId) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.id", hasItem(eventhubId));
    }
}
