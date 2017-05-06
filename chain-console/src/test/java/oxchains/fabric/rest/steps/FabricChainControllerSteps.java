package oxchains.fabric.rest.steps;

import io.restassured.module.mockmvc.response.MockMvcResponse;
import net.thucydides.core.annotations.Step;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsMapContaining;
import org.hyperledger.fabric.sdk.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.sdk.FabricSDK;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.util.stream.Collectors.toList;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class FabricChainControllerSteps {

    @Autowired private FabricSDK fabricSDK;
    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;

    private MockMvcResponse mockMvcResponse;

    @Step("get default chain info")
    public void getChain() {
        mockMvcResponse = given()
          .when()
          .get("/chain");
    }

    @Step("info includes {0}")
    public void infoIncludes(String... params) {
        assertNotNull(params);
        Matcher[] matchers = new Matcher[params.length];
        Arrays
          .stream(params)
          .map(param -> IsMapContaining.hasKey(containsString(param)))
          .collect(toList())
          .toArray(matchers);

        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .body("data", allOf(matchers));
    }

    @Step("info includes list that contains {0}")
    public void infoIncludesInList(String... params) {
        assertNotNull(params);
        Matcher[] matchers = new Matcher[params.length];
        Arrays
          .stream(params)
          .map(param -> IsMapContaining.hasKey(containsString(param)))
          .collect(toList())
          .toArray(matchers);

        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .body("data", everyItem(allOf(matchers)));
    }

    @Step("{0} at {1} joining chain")
    public void joinChain(String peerId, String peerEndpoint) {
        if(fabricSDK.getPeer(peerId).isPresent()) return;

        Optional<Peer> peerOptional = fabricSDK.withPeer(peerId, propertyParse(peerEndpoint, testProperties));
        assertTrue(peerOptional.isPresent());
        boolean joined = fabricSDK.joinChain(peerOptional.get());
        assertTrue("peer should have joined default chain", joined);
    }

    @Step("get chain blocks")
    public void getChainblocks() {
        mockMvcResponse = given()
          .when()
          .get("/chain/block");
    }

    @Step("info includes {1} in key {0}")
    public void infoIncludesInKey(String key, String... params) {
        assertNotNull(params);
        Matcher[] matchers = new Matcher[params.length];
        Arrays
          .stream(params)
          .map(param -> IsMapContaining.hasKey(containsString(param)))
          .collect(toList())
          .toArray(matchers);

        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .body("data." + key, everyItem(allOf(matchers)));
    }

    @Step("get block {0} of default chain")
    public void getChainblock(long blocknumber) {
        mockMvcResponse = given()
          .queryParam("number", blocknumber)
          .when()
          .get("/chain/block");
    }

}
