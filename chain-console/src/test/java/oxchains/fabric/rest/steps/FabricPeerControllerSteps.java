package oxchains.fabric.rest.steps;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import net.thucydides.core.annotations.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.console.data.UserRepo;
import oxchains.fabric.console.domain.PeerEventhub;
import oxchains.fabric.console.domain.User;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class FabricPeerControllerSteps {

    Logger LOG = LoggerFactory.getLogger(getClass());

    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;
    private MockMvcResponse mockMvcResponse;
    private String token;

    @Step("fetching peers for default chain")
    public void getPeers() {
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
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

    @Step("adding peer {0}:{1} and eventhub {2} with pass {3}")
    public void addPeerWithEventhub(String peerId, String peerEndpoint, String eventHubEndpoint, String pass) throws Exception {
        PeerEventhub peerInfo = new PeerEventhub();
        peerInfo.setId(peerId);
        peerInfo.setEndpoint(propertyParse(peerEndpoint, testProperties));
        peerInfo.setEventhub(propertyParse(eventHubEndpoint, testProperties));
        peerInfo.setPassword(pass);
        ObjectMapper mapper = new ObjectMapper().disable(MapperFeature.USE_ANNOTATIONS);
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .contentType(JSON.withCharset(UTF_8))
          .body(mapper.writeValueAsString(peerInfo))
          .when()
          .post("/peer");
    }

    public void operationDone() {
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

    @Step("peer {0} is not connected yet")
    public void peerNotConnected(String peerId) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.connected", not(is("1")));
    }

    @Autowired private UserRepo userRepo;

    @Step("enrolled {0} of {1}")
    public void enrolledAdmin(String username, String affiliation) throws Exception {
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

        Optional<User> enrolledUserOptional = userRepo.findUserByUsernameAndAffiliation(username, affiliation);
        assertTrue("given admin should have been registered", userOptional.isPresent());
        LOG.info("admin certificate: \n{}", enrolledUserOptional
          .get()
          .getCertificate());
    }

    public void connectPeer(String peerId) throws Exception {
        TimeUnit.MINUTES.sleep(2);
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .when()
          .post("/peer/" + peerId + "/connection");
    }

    public void peerInListConnected(String peerId) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data", hasItem(allOf(hasEntry("connected", "1"), hasEntry("id", peerId))));
    }

    @Step("enroll peer {0} of org {2} with pass {1}")
    public void enrollPeer(String peerId, String pass, String org, String ca, String cauri, String msp) throws Exception {
        PeerEventhub peer = new PeerEventhub();
        peer.setId(peerId);
        peer.setAffiliation(org);
        peer.setPassword(pass);
        peer.setCa(ca);
        peer.setUri(propertyParse(cauri, testProperties));
        peer.setMsp(msp);
        ObjectMapper mapper = new ObjectMapper().disable(MapperFeature.USE_ANNOTATIONS);
        mockMvcResponse = given()
          .contentType(JSON)
          .body(mapper.writeValueAsString(peer))
          .when()
          .post("/peer/enrollment");
    }

    @Step("peer {0} enrolled")
    public void enrolledPeer(String peerId) {
        Map<String, String> peer = mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("data.id", is(peerId))
          .and()
          .extract()
          .path("data");
        LOG.info("peer certificate: \n{}", peer.get("certificate"));
        String privateKey = peer.get("privateKey");
        Iterator<String> keyIter = Splitter
          .fixedLength(64)
          .split(privateKey)
          .iterator();
        StringBuilder key= new StringBuilder();
        while (keyIter.hasNext()) {
            key.append(keyIter.next()).append("\n");
        }
        LOG.info("private key: \n-----BEGIN PRIVATE KEY-----\n{}-----END PRIVATE KEY-----", key.toString());
    }

}
