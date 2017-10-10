package oxchains.fabric.rest.stories;

import net.thucydides.core.annotations.Steps;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import oxchains.fabric.rest.steps.FabricChainControllerSteps;

import java.util.Arrays;

import static java.util.stream.Collectors.toList;

/**
 * @author aiet
 */
public class FabricChainControllerStory {

    @Steps FabricChainControllerSteps steps;

    @Given("chain admin $username of org $org enrolled")
    public void givenChainAdmin(String username, String affiliation) throws Exception {
        steps.chainAdminEnrolled(username, affiliation);
    }

    @Given("chain configuration $chainConfig")
    public void givenChainConfiguration(String chainConfig) {
        steps.chainConfig(chainConfig);
    }

    @Given("chain peer $peerId at $peerEndpoint with eventhub at $eventhub, password $pass added from org $org, ca $ca at $caUri with msp $msp")
    public void givenPeerAdded(String peerId, String peerEndpoint, String eventhub, String pass, String org, String ca, String caUri, String msp) throws Exception {
        steps.addPeer(peerId, peerEndpoint, eventhub, pass, org, ca, caUri, msp);
    }

    @Given("chain $chainname constructed")
    public void givenChain(String chainname){
        steps.createChain(chainname);
        steps.chainConstructed();
    }


    @Given("chain peer $peerId joined $chainname and listened")
    public void givenPeerJoinedChain(String peerId, String chainname){
        steps.peerJoins(peerId, chainname);
        steps.peerJoined(peerId, chainname);
    }

    @When("chain peer $peerId joins $chainname")
    public void whenPeerJoinsChain(String peerId, String chainname){
        steps.peerJoins(peerId, chainname);
    }

    @Then("chain peer $peerId has joined $chainname")
    public void thenPeerJoined(String peerId, String chainname){
        steps.peerJoined(peerId, chainname);
    }

    @When("construct chain $chainName")
    public void whenConstructChain(String chainName) {
        steps.createChain(chainName);
    }

    @Then("$chainName constructed")
    public void thenChainConstructed(String chainName) {
        steps.chainConstructed();
    }

    @When("I get chain info of $chainName")
    public void whenIGetChainInfo(String chainName) {
        steps.getChain(chainName);
    }

    @Then("there is nothing on the chain")
    public void thenNothingOnChain(){
        steps.operationFail();
    }

    @Then("there are $height, $hash in chain")
    public void thenInChainHas(String height, String hash) {
        steps.infoIncludes(height, hash);
    }

    @When("I get block of chain $chainName")
    public void whenIGetChainBlock(String chainName) {
        steps.getChainblocks(chainName);
    }

    @Then("there are $size, $hash, $previous in block")
    public void thenInBlockHas(String size, String hash, String previous) {
        steps.infoIncludesInList(size, hash, previous);
    }

    @Then("there are $params in block $datalist")
    public void thenInBlockDataListHas(String params, String datalist) {
        String[] paramArr = params.split(",");
        steps.infoIncludesInKey(datalist, Arrays
          .stream(paramArr)
          .map(String::trim)
          .collect(toList())
          .toArray(paramArr));
    }

    @When("I get block $blocknumber of chain $chainName")
    public void whenIGetChainBlock(long blocknumber, String chainname) {
        steps.getChainblock(chainname, blocknumber);
    }

}
