package oxchains.fabric.console.domain;

import org.hyperledger.fabric.protos.peer.Query;

/**
 * @author aiet
 */
public class ChaincodeInfo {

    public ChaincodeInfo() {
    }

    public ChaincodeInfo(Query.ChaincodeInfo chaincode) {
        this.input = chaincode.getInput();
        this.path = chaincode.getPath();
        this.version = chaincode.getVersion();
        this.init = chaincode.isInitialized();
        this.error = chaincode.getInitializationErrorString();
        this.name = chaincode.getName();
    }

    private String name;
    private String input;
    private String path;
    private String version;
    private boolean init;
    private String error;

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

    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
