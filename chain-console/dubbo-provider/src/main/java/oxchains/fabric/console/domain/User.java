package oxchains.fabric.console.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author aiet
 */
@Entity
public class User implements Serializable{

    public User() {
    }

    public User(String username, String affiliation) {
        this.username = username;
        this.affiliation = affiliation;
    }

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    @ElementCollection(fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<String> authorities = new HashSet<>();
    private String affiliation;
    private String msp;
    private String ca;
    private String uri;
    @JsonIgnore
    @Column(length = 1024)
    private String privateKey;

    @Column(length = 1024) private String certificate;

    public void inheritMSP(User user) {
        setCa(user.getCa());
        setUri(user.getUri());
        setMsp(user.getMsp());
        setAffiliation(user.getAffiliation());
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public String getMsp() {
        return msp;
    }

    public void setMsp(String msp) {
        this.msp = msp;
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
    public String getUri() {
        return uri;
    }

    @JsonSetter
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCa() {
        return ca;
    }

    public void setCa(String ca) {
        this.ca = ca;
    }

    @JsonIgnore private Date createtime = new Date();

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonSetter
    public void setPassword(String password) {
        this.password = password;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    @Override
    public String toString() {
        return String.format("%s of %s [%s]", username, affiliation, id);
    }

    @JsonIgnore
    public boolean enrolled() {
        return !isEmpty(privateKey) && !isEmpty(certificate);
    }
}
