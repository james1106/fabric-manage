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

    @Given("fabric console for chain")
    public void givenFabricConsoleForChain() {
    }

    @Given("$peerId at $peerEndpoin joined chain")
    public void givenChainHasPeer(String peerId, String peerEndpoint) {
        steps.joinChain(peerId, peerEndpoint);
    }

    @When("I get chain info")
    public void whenIGetChainInfo() {
        steps.getChain();
    }

    @Then("there are $height, $hash in chain")
    public void thenInChainHas(String height, String hash) {
        steps.infoIncludes(height, hash);
    }

    @When("I get chain block")
    public void whenIGetChainBlock() {
        steps.getChainblocks();
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

    @When("I get block $blocknumber of chain")
    public void whenIGetChainBlock(long blocknumber) {
        steps.getChainblock(blocknumber);
    }

}
