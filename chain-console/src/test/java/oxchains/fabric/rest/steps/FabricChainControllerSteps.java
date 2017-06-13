package oxchains.fabric.rest.steps;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import net.thucydides.core.annotations.Step;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsMapContaining;
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

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class FabricChainControllerSteps {

    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;

    private Logger LOG = LoggerFactory.getLogger(getClass());

    private File chainConfigFile;

    private MockMvcResponse mockMvcResponse;

    @Step("get chain {0} info")
    public void getChain(String chainname) {
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .when()
          .get("/chain/" + chainname);
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

    @Step("get chain {0}'s blocks")
    public void getChainblocks(String chain) {
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .when()
          .get("/chain/" + chain + "/block");
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

    @Step("get block {1} of chain {0}")
    public void getChainblock(String chainname, long blocknumber) {
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .queryParam("number", blocknumber)
          .when()
          .get("/chain/" + chainname + "/block");
    }

    @Step("with chain config {0}")
    public void chainConfig(String chainConfig) {
        chainConfigFile = new File(String.format("src/test/resources/chain_configuration/%s.tx", chainConfig));
    }

    @Step("create chain {0}")
    public void createChain(String chainName) {
        try {
            TimeUnit.SECONDS.sleep(90);
        } catch (Exception ignored) {
        }

        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .contentType(MULTIPART_FORM_DATA_VALUE)
          .multiPart("config", chainConfigFile)
          .queryParam("chain", chainName)
          .when()
          .post("/chain");
    }

    public void chainConstructed() {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .and()
          .body("status", is(1));
    }

    @Autowired private UserRepo userRepo;

    private String token;

    public void chainAdminEnrolled(String username, String affiliation) throws Exception {
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

    public void operationFail() {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .body("status", is(-1));
    }

    @Step("add peer {0} at {1}, listening on {2}, with pass {3}")
    public void addPeer(String peerId, String peerEndpoint, String eventhub, String pass, String org, String ca, String cauri, String msp) throws Exception {
        PeerEventhub peerInfo = new PeerEventhub();
        peerInfo.setId(peerId);
        peerInfo.setEndpoint(propertyParse(peerEndpoint, testProperties));
        peerInfo.setEventhub(propertyParse(eventhub, testProperties));
        peerInfo.setPassword(pass);
        ObjectMapper mapper = new ObjectMapper().disable(MapperFeature.USE_ANNOTATIONS);

        given()
          .header(AUTHORIZATION, "Bearer " + token)
          .contentType(JSON.withCharset(UTF_8))
          .body(mapper.writeValueAsString(peerInfo))
          .when()
          .post("/peer")
          .then()
          .statusCode(SC_OK)
          .and()
          .body("status", is(1));

        peerInfo.setAffiliation(org);
        peerInfo.setCa(ca);
        peerInfo.setUri(propertyParse(cauri, testProperties));
        peerInfo.setMsp(msp);

       Map<String, String> peer = given()
          .contentType(JSON)
          .body(mapper.writeValueAsString(peerInfo))
          .when()
          .post("/peer/enrollment")
         .then().statusCode(SC_OK).body("status", is(1))
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
        StringBuilder key = new StringBuilder();
        while (keyIter.hasNext()) {
            key
              .append(keyIter.next())
              .append("\n");
        }
        LOG.info("private key: \n-----BEGIN PRIVATE KEY-----\n{}-----END PRIVATE KEY-----", key.toString());
    }

    @Step("peer {0} joins chain {1}")
    public void peerJoins(String peerId, String chainname) {
        mockMvcResponse = given()
          .header(AUTHORIZATION, "Bearer " + token)
          .queryParam("peer", peerId)
          .when()
          .post("/chain/" + chainname + "/peer");
    }

    @Step("peer {0} has joined chain {1}")
    public void peerJoined(String peerId, String chainname) {
        mockMvcResponse
          .then()
          .statusCode(SC_OK)
          .body("status", is(1));
    }

}
