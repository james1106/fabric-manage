package oxchains.fabric.console.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author aiet
 */
@Entity
public class PeerEventhub implements Serializable{

    @Id private String id;
    private String endpoint;
    private String eventhub;
    private String password;
    private Date createtime = new Date();
    private String affiliation;
    private String msp;
    private String ca;
    private String uri;

    @Column(length = 1024) private String privateKey;

    @Column(length = 1024) private String certificate;

    public void inheritMSP(User user) {
        setCa(user.getCa());
        setUri(user.getUri());
        setMsp(user.getMsp());
        setAffiliation(user.getAffiliation());
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getMsp() {
        return msp;
    }

    public void setMsp(String msp) {
        this.msp = msp;
    }

    public String getCa() {
        return ca;
    }

    public void setCa(String ca) {
        this.ca = ca;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonSetter
    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEventhub() {
        return eventhub;
    }

    public void setEventhub(String eventhub) {
        this.eventhub = eventhub;
    }

    @Override
    public String toString() {
        return String.format("%s@%s, listening on %s", id, endpoint, eventhub);
    }

    @Override
    public boolean equals(Object another) {
        if (another == null) return false;
        if (another instanceof PeerEventhub && this.id != null) {
            PeerEventhub anotherInstance = (PeerEventhub) another;
            return anotherInstance.id != null && anotherInstance.affiliation != null && this.id.equals(anotherInstance.id) && this.affiliation.equals(anotherInstance.affiliation);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (id + affiliation).hashCode();
    }

    @JsonIgnore
    public boolean enrolled() {
        return !isEmpty(privateKey) && !isEmpty(certificate);
    }
}
