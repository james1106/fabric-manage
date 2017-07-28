package oxchains.fabric.sdk.domain;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import oxchains.fabric.sdk.FabricConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.PrivateKey;
import java.security.Security;
import java.util.Set;

/**
 * @author aiet
 */
public class CAUser implements User {

    private String name;
    private Set<String> roles;
    private String affiliation;
    private Enrollment enrollment;
    private String mspId;
    private String password;

    public CAUser(String name, String affiliation, String mspId, String password) {
        this.name = name;
        this.affiliation = affiliation;
        this.mspId = mspId;
        this.password = password;
    }

    public CAUser(String name, String affiliation, String mspId) {
        this.name = name;
        this.affiliation = affiliation;
        this.mspId = mspId;
    }

    public static CAUser fromUser(oxchains.fabric.console.domain.User u){
        CAUser user = new CAUser(u.getUsername(), u.getAffiliation(), u.getMsp());
        if(u.getPrivateKey()!=null && u.getCertificate()!=null) {
            user.setEnrollment(new CAEnrollment(u.getPrivateKey(), u.getCertificate()));
        }
        user.roles = u.getAuthorities();
        user.setPassword(u.getPassword());
        return user;
    }

    public static CAUser fromUser2(oxchains.fabric.console.domain.User u){
        CAUser user = null;
        try {
            // TODO hardcode
            String certificate = new String(IOUtils.toByteArray(new FileInputStream(FabricConfig.FABRIC_RESOURCE_PATH + FabricConfig.FABRIC_ADMIN_CERTIFICATE_PATH)), "UTF-8");
            String privateKeyFile = FabricConfig.FABRIC_RESOURCE_PATH + FabricConfig.FABRIC_ADMIN_PRIVATEKEY_PATH;
            final PEMParser pemParser = new PEMParser(new StringReader(new String(IOUtils.toByteArray(new FileInputStream(privateKeyFile)))));

            PrivateKeyInfo pemPair = (PrivateKeyInfo) pemParser.readObject();

            Security.addProvider(new BouncyCastleProvider());
            PrivateKey privateKey = new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPrivateKey(pemPair);
            user = new CAUser(u.getUsername(), u.getAffiliation(), u.getMsp());
            if(u.getPrivateKey()!=null && u.getCertificate()!=null) {
                user.setEnrollment(new CAEnrollment(privateKey, certificate));
            }
            user.roles = u.getAuthorities();
            user.setPassword(u.getPassword());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword(){
        return this.password;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<String> getRoles() {
        return this.roles;
    }

    @Override
    public String getAccount() {
        return this.name;
    }

    @Override
    public String getAffiliation() {
        return affiliation;
    }

    @Override
    public Enrollment getEnrollment() {
        return enrollment;
    }

    @Override
    public String getMSPID() {
        return mspId;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

}
