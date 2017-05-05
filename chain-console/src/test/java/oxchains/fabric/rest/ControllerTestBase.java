package oxchains.fabric.rest;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import net.serenitybdd.jbehave.SerenityStory;
import org.jbehave.core.annotations.BeforeStories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.console.rest.FabricChainController;
import oxchains.fabric.console.rest.FabricPeerController;
import oxchains.fabric.console.rest.FabricUserController;
import oxchains.fabric.console.service.ChainService;
import oxchains.fabric.console.service.PeerService;
import oxchains.fabric.console.service.UserService;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class ControllerTestBase extends SerenityStory {

    @Autowired private UserService userService;
    @Autowired private PeerService peerService;
    @Autowired private ChainService chainService;

    @BeforeStories
    public void init() {
        RestAssuredMockMvc.standaloneSetup(
          new FabricUserController(userService),
          new FabricPeerController(peerService),
          new FabricChainController(chainService)
        );
    }

}
