package oxchains.fabric.rest;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import net.serenitybdd.jbehave.SerenityStory;
import org.jbehave.core.annotations.BeforeStory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.console.rest.FabricUserController;
import oxchains.fabric.console.service.UserService;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class FabricUserControllerTest extends SerenityStory {

    @Autowired private UserService userService;

    @BeforeStory
    public void init() {
        RestAssuredMockMvc.standaloneSetup(new FabricUserController(userService));
    }

}
