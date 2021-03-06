package oxchains.fabric.sdk.domain;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

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
