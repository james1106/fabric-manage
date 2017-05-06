package oxchains.fabric.console.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hyperledger.fabric.protos.peer.Query;

import javax.persistence.*;

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

    private int installed;

    public ChainCodeInfo(String name, String version, String lang, String path) {
        this.name = name;
        this.version = version;
        this.path = path;
        this.lang = lang;
    }

    public int getInstalled() {
        return installed;
    }

    public void setInstalled(int installed) {
        this.installed = installed;
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
}
