package oxchains.fabric.sdk.steps;

import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.RandomStringUtils;
import org.hyperledger.fabric.sdk.Chain;
import org.hyperledger.fabric.sdk.ChainConfiguration;
import org.hyperledger.fabric.sdk.Orderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.sdk.FabricSDK;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static oxchains.fabric.util.StoryTestUtil.propertyParse;

/**
 * @author aiet
 */

@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class ChainAPISteps {

    @Autowired private FabricSDK sdk;
    private ChainConfiguration chainConfiguration;
    private Orderer orderer;

    @Value("#{${fabric.test.endpoints}}") private Map<String, String> testProperties;

    @Step("given fabric sdk client created")
    public void createFabricClient() {
        assertNotNull("sdk should not be null", sdk);
    }

    @Step("given orderer endpoint {0}")
    public void buildOrdererFrom(String ordererEndpoint) {
        Optional<Orderer> ordererOptional = sdk.withOrderer(RandomStringUtils.randomAscii(7), propertyParse(ordererEndpoint, testProperties));
        assertTrue(ordererOptional.isPresent());
        orderer = ordererOptional.get();
    }

    @Step("when construct a chain {0}")
    public void constructChainOf(String chainName) throws IOException {
        chainConfiguration = new ChainConfiguration(new File(String.format("src/test/resources/chain_configuration/%s.tx", chainName)));
        Optional<Chain> chainOptional = sdk.constructChain(chainName, orderer, chainConfiguration);
        assertTrue(chainOptional.isPresent());
    }

    @Step("then chain {0} should have been created")
    public void chainCreated(String chainName) {
        Optional<Chain> chainOptional = sdk.getChain(chainName);
        assertTrue(chainOptional.isPresent());
    }

}
