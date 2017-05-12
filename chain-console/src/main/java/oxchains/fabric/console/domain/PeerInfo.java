package oxchains.fabric.console.domain;

import org.hyperledger.fabric.sdk.Peer;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * @author aiet
 */
public class PeerInfo {

    private String id;
    private String endpoint;
    private String status = "N/A";
    private int statusCode = -1;

    private List<ChainCodeInfo> chaincodes = emptyList();
    private List<ChainCodeInfo> runnablecodes = emptyList();
    private List<String> chains = emptyList();

    public PeerInfo() {
    }

    public PeerInfo(Peer peer) {
        this.id = peer.getName();
        this.endpoint = peer.getUrl();
    }

    public List<ChainCodeInfo> getRunnablecodes() {
        return runnablecodes;
    }

    public void setRunnablecodes(List<ChainCodeInfo> runnablecodes) {
        this.runnablecodes = runnablecodes;
    }

    public List<ChainCodeInfo> getChaincodes() {
        return chaincodes;
    }

    public void setChaincodes(List<ChainCodeInfo> chaincodes) {
        this.chaincodes = chaincodes;
    }

    public List<String> getChains() {
        return chains;
    }

    public void setChains(List<String> chains) {
        this.chains = chains;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        if(statusCode==1) this.status = "CONNECTED";
        else this.status = "UNREACHABLE";
    }

    @Override
    public String toString() {
        return String.format("%s - %s", id, endpoint);
    }

}
