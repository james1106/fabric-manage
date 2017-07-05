package oxchains.fabric.sdk;

import org.springframework.beans.factory.annotation.Value;

/**
 * FabricConfig
 *
 * @author liuruichao
 * Created on 2017/7/3 13:39
 */
public final class FabricConfig {
    @Value("fabric.resource.path")
    public static String FABRIC_RESOURCE_PATH;

    @Value("fabric.admin.privatekey.path")
    public static String FABRIC_ADMIN_PRIVATEKEY_PATH;

    @Value("fabric.admin.certificate.path")
    public static String FABRIC_ADMIN_CERTIFICATE_PATH;
}