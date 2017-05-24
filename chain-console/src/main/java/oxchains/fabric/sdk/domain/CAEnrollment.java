package oxchains.fabric.sdk.domain;

import org.hyperledger.fabric.sdk.Enrollment;
import oxchains.fabric.console.domain.User;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * @author aiet
 */
public class CAEnrollment implements Enrollment {
    private String cert;
    private PrivateKey privateKey;

    public CAEnrollment(User user) {
        this.cert = user.getCertificate();
        try {
            KeyFactory kf = KeyFactory.getInstance("EC");
            PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(Base64
              .getDecoder()
              .decode(user.getPrivateKey()));
            this.privateKey = kf.generatePrivate(specPriv);
        } catch (Exception ignore) {
        }
    }

    @Override
    public PrivateKey getKey() {
        return privateKey;
    }

    @Override
    public String getCert() {
        return cert;
    }
}
