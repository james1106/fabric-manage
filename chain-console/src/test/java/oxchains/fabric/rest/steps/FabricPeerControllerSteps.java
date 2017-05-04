package oxchains.fabric.rest.steps;

import io.restassured.module.mockmvc.response.MockMvcResponse;
import net.thucydides.core.annotations.Step;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.console.domain.PeerInfo;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
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

    @Step("no peer on default chain")
    public void noPeerYet() {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data", empty());
    }

    @Step("adding peer {0}:{1} and eventhub {2}")
    public void addPeerWithEventhub(String peerId, String peerEndpoint, String eventHubEndpoint) {
        PeerInfo peerInfo = new PeerInfo();
        peerInfo.setId(peerId);
        peerInfo.setEndpoint(propertyParse(peerEndpoint, testProperties));
        peerInfo.setEventEndpoint(propertyParse(eventHubEndpoint, testProperties));
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

}
