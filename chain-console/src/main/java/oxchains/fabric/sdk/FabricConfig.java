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
    public static String RESOURCE_PATH;
}