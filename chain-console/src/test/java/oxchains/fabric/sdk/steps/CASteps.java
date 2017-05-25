package oxchains.fabric.sdk.steps;

import net.thucydides.core.annotations.Step;
import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import oxchains.fabric.ChainConsoleApplication;
import oxchains.fabric.sdk.FabricSDK;
import oxchains.fabric.sdk.domain.CAEnrollment;
import oxchains.fabric.sdk.domain.CAUser;

/**
 * @author aiet
 */
@ContextConfiguration(classes = ChainConsoleApplication.class)
@TestPropertySource(locations = "classpath:test.properties")
public class CASteps {

    @Autowired
    FabricSDK fabricSDK;

    @Value("${fabric.ca.server.admin}") private String caServerAdmin;
    @Value("${fabric.ca.server.admin.pass}") private String caServerAdminPass;

    @Value("${fabric.ca.server.name}") private String caName;
    @Value("${fabric.ca.server.url}") private String caUri;

    @Value("${fabric.ca.server.admin.mspid}") private String msp;
    @Value("${fabric.ca.server.admin.affiliation}") private String affiliation;

    @Value("${fabric.ca.server.admin.key}") private String key;
    @Value("${fabric.ca.server.admin.cert}") private String cert;

    @Step("prepare admin user context")
    public void adminContext() throws Exception {
        CAUser user = new CAUser(caServerAdmin, affiliation, msp, caServerAdminPass);
        String keyStr =  IOUtils.toString(new ClassPathResource(key).getInputStream());
        String certStr =  IOUtils.toString(new ClassPathResource(cert).getInputStream());
        user.setEnrollment(new CAEnrollment(keyStr, certStr));
        fabricSDK.withUserContext(user);
    }
}
