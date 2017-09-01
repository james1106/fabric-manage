package oxchains.fabric.sdk.steps;

import net.thucydides.core.annotations.Step;
import org.hyperledger.fabric.protos.peer.Query.ChaincodeInfo;
import org.hyperledger.fabric.sdk.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.console.domain.ChainCodeInfo;
import oxchains.fabric.sdk.FabricSDK;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hyperledger.fabric.sdk.ChaincodeResponse.Status.FAILURE;
import static org.hyperledger.fabric.sdk.ChaincodeResponse.Status.SUCCESS;
import static org.junit.Assert.*;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class ChaincodeAPISteps {

    @Autowired private FabricSDK fabricSDK;
    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;
    private ProposalResponse installResponse;
    private ProposalResponse instantiateResponse;
    private ProposalResponse invokeResponse;
    private ProposalResponse queryResponse;
    private ChaincodeID chaincode;
    private Channel chain;
    private List<ChaincodeInfo> installedChaincodes = new ArrayList<>(2);

    @Step("load chaincode from path {0}")
    public void loadChaincode(String chaincodeName, String chaincodeVersion, String chaincodeLocation) {
        chaincode = ChaincodeID
          .newBuilder()
          .setName(chaincodeName)
          .setVersion(chaincodeVersion)
          .setPath(chaincodeLocation)
          .build();
    }

    @Step("install chaincode on chain {0}")
    public void installChaincodeOnPeer(String chainName) {
        checkChain(chainName);
        installResponse = fabricSDK.installChaincodeOnPeer(chaincode, chain, "go", "src/test/resources/chaincode", chain.getPeers()).get(0);
        assertNotNull(installResponse);
    }

    @Step("chaincode installed successfully")
    public void installationSucceeded() {
        assertEquals(SUCCESS, installResponse.getStatus());
    }

    @Step("instantiate chaincode on chain {0} with {1}")
    public void instantiateWith(String chaincodeName, String arg) throws Exception {
        Optional<ChaincodeID> chaincodeIdOptional = fabricSDK.getChaincode(chaincodeName);
        assertTrue("chaincode should have been created", chaincodeIdOptional.isPresent());
        chaincode = chaincodeIdOptional.get();

        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(new File("src/test/resources/chain_configuration/" + chaincodeName + "_endorsement_policy.yaml"));

        instantiateResponse = fabricSDK
          .instantiateChaincode(chaincode, chain, chain
            .getPeers()
            .iterator()
            .next(), chaincodeEndorsementPolicy, arg.split(" "))
          .get(60, SECONDS);
        assertNotNull(instantiateResponse);
    }

    @Step("chaincode instantiated successfully")
    public void instantiationSucceeded() {
        assertEquals(SUCCESS, instantiateResponse.getStatus());
    }

    @Step("query on chain with {0}")
    public void queryChainWith(ChainCodeInfo chainCodeInfo, String arg) {
        assertNotNull("chain should have been created", chain);
        assertNotNull("chaincode should have been created", chaincode);
        queryResponse = fabricSDK.queryChaincode(chainCodeInfo, chaincode, chain, chain
          .getPeers()
          .iterator()
          .next(), arg.split(" "));
    }

    @Step("query result: {0}")
    public void resultIs(int result) {
        assertEquals(SUCCESS, queryResponse.getStatus());
        String responseResult = queryResponse
          .getProposalResponse()
          .getResponse()
          .getPayload()
          .toStringUtf8();
        assertEquals("" + result, responseResult);
    }

    @Step("query result contains: {0}")
    public void resultContains(String invoiceId) {
        assertEquals(SUCCESS, queryResponse.getStatus());
        String responseResult = queryResponse
          .getProposalResponse()
          .getResponse()
          .getPayload()
          .toStringUtf8();
        assertThat(responseResult, containsString(invoiceId));
    }

    @Step("query result does not contain: {0}")
    public void resultContainsNo(String invoiceId) {
        assertEquals(SUCCESS, queryResponse.getStatus());
        String responseResult = queryResponse
          .getProposalResponse()
          .getResponse()
          .getPayload()
          .toStringUtf8();
        assertThat(responseResult, not(containsString(invoiceId)));
    }

    @Step("invoke chain with arg {0}")
    public void invokeChainWith(ChainCodeInfo chainCodeInfo, String arg) throws Exception {
        assertNotNull("chain should have been created", chain);
        assertNotNull("chaincode should have been created", chaincode);
        invokeResponse = fabricSDK
          .invokeChaincode(chainCodeInfo, chaincode, chain, chain
            .getPeers()
            .iterator()
            .next(), arg.split(" "))
          .get(60, SECONDS);
        assertNotNull(invokeResponse);
    }

    @Step("chain invocation succceeded")
    public void invokeSucceeded() {
        assertEquals(SUCCESS, invokeResponse.getStatus());
    }

    @Step("chain {0} listens on event hub at {1}")
    public void chainWithEventHub(String eventName, String eventEndpoint) throws Exception {
        Optional<EventHub> eventHubOptional = fabricSDK.withEventHub(eventName, propertyParse(eventEndpoint, testProperties));
        assertTrue(eventHubOptional.isPresent());

        /* append event hub and re-initialize */
        chain.addEventHub(eventHubOptional.get());
        chain.initialize();
    }

    @Step("check chain {0} existence")
    public void checkChain(String chainName) {
        Optional<Channel> chainOptional = fabricSDK.getChain(chainName);
        assertTrue("chain should have been created", chainOptional.isPresent());
        /* append event hub and re-initialize */
        chain = chainOptional.get();
    }

    @Step("check chain {0} existence")
    public void checkChaincode(String chaincodeName) {
        Optional<ChaincodeID> chaincodeOptional = fabricSDK.getChaincode(chaincodeName);
        assertTrue("chaincode should have been created", chaincodeOptional.isPresent());
        /* append event hub and re-initialize */
        chaincode = chaincodeOptional.get();
    }

    @Step("chain invocation failed")
    public void invokeFailed() {
        assertEquals(FAILURE, invokeResponse.getStatus());
    }

    @Step("chain query failed")
    public void queryFail() {
        assertNull(queryResponse);
    }

    @Step("query {0} installed chaincodes")
    public void queryChaincodeOfPeer(String peerId) {
        Optional<Peer> peerOptional = fabricSDK.getPeer(peerId);
        assertTrue("peer should have been created", peerOptional.isPresent());
        installedChaincodes.addAll(fabricSDK.chaincodesOnPeer(peerOptional.get()));
    }

    @Step("peer has installed chaincode {0}")
    public void peerHasInstalled(String chaincodeName) {
        Optional<ChaincodeInfo> chaincodeInfoOptional = installedChaincodes
          .stream()
          .filter(cc -> chaincodeName.equals(cc.getName()))
          .findFirst();
        assertTrue("peer should have installed chaincode " + chaincodeName, chaincodeInfoOptional.isPresent());
    }

    @Step("query instantiated chaincodes on {0}")
    public void queryInstantiatedChaincodeOfPeer(String peerId, String chainName) {
        checkChain(chainName);
        Optional<Peer> peerOptional = fabricSDK.getPeer(peerId);
        assertTrue("peer should have been created", peerOptional.isPresent());
        installedChaincodes.clear();
        installedChaincodes.addAll(fabricSDK.chaincodesOnPeer(peerOptional.get(), chain));
    }
}
