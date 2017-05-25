package oxchains.fabric.console.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hyperledger.fabric.protos.peer.Query;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author aiet
 */
@Entity
public class ChainCodeInfo {

    public ChainCodeInfo() {
    }

    public ChainCodeInfo(Query.ChaincodeInfo chaincode) {
        this.input = chaincode.getInput();
        this.path = chaincode.getPath();
        this.version = chaincode.getVersion();
        this.error = chaincode.getInitializationErrorString();
        this.name = chaincode.getName();
    }

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Transient
    private String input;
    @JsonIgnore
    @Transient
    private String error;

    private String name;

    private String path;
    private String version;
    private String lang;
    private Date createtime;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> installed = new HashSet<>(4);

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> instantiated = new HashSet<>(4);

    public ChainCodeInfo(String name, String version, String lang, String path) {
        this.name = name;
        this.version = version;
        this.path = path;
        this.lang = lang;
        this.createtime = new Date();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Set<String> getInstantiated() {
        return instantiated;
    }

    public void setInstantiated(Set<String> instantiated) {
        this.instantiated.clear();
        this.instantiated.addAll(instantiated);
    }

    public Set<String> getInstalled() {
        return installed;
    }

    public void setInstalled(Set<String> installed) {
        this.installed.clear();
        this.installed.addAll(installed);
    }

    public void addInstalled(String installedPeer) {
        this.installed.add(installedPeer);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void addInstantiated(String peer) {
        this.instantiated.add(peer);
    }
}
