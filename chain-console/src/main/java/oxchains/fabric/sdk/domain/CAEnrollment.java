package oxchains.fabric.sdk.domain;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
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
        this(user.getCertificate(), user.getPrivateKey());
    }

    public CAEnrollment(String key, String cert) {
        this.cert = cert;

        try {
            KeyFactory kf = KeyFactory.getInstance("ECDSA");
            PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(Base64
              .getDecoder()
              .decode(key));
            this.privateKey = kf.generatePrivate(specPriv);
        } catch (Exception ignore) {
            ignore.printStackTrace();
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
