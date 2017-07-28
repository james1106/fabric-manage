package oxchains.fabric.rest;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import net.serenitybdd.jbehave.SerenityStory;
import org.jbehave.core.annotations.BeforeStories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import oxchains.fabric.ChainConsoleApplication;

import javax.servlet.Filter;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test.properties")
public class ControllerTestBase extends SerenityStory {

    @Autowired private WebApplicationContext context;

    @Autowired private Filter springSecurityFilterChain;

    @BeforeStories
    public void init() {
        RestAssuredMockMvc.mockMvc(webAppContextSetup(context)
          .addFilter(springSecurityFilterChain)
          .build());
    }

}
