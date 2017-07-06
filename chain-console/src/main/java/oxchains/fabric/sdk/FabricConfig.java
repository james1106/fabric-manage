package oxchains.fabric.sdk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * FabricConfig
 *
 * @author liuruichao
 * Created on 2017/7/3 13:39
 */
@Component
public final class FabricConfig {
    public static String FABRIC_RESOURCE_PATH;

    public static String FABRIC_ADMIN_PRIVATEKEY_PATH;

    public static String FABRIC_ADMIN_CERTIFICATE_PATH;

    @Value("${fabric.resource.path}")
    public void setResourcePath(String resourcePath) {
        FABRIC_RESOURCE_PATH = resourcePath;
    }

    @Value("${fabric.admin.privatekey.path}")
    public void setAdminPrivateKeyPath(String adminPrivateKeyPath) {
        FABRIC_ADMIN_PRIVATEKEY_PATH = adminPrivateKeyPath;
    }

    @Value("${fabric.admin.certificate.path}")
    public void setAdminCertificatePath(String adminCertificatePath) {
        FABRIC_ADMIN_CERTIFICATE_PATH = adminCertificatePath;
    }
}