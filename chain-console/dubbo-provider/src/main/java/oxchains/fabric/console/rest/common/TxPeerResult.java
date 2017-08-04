package oxchains.fabric.console.rest.common;

import java.io.Serializable;

/**
 * @author aiet
 */
public class TxPeerResult implements Serializable{

    private final String txid;
    private final String peer;
    private final int success;

    public TxPeerResult(String txid, String peer, int success) {
        this.txid = txid;
        this.peer = peer;
        this.success = success;
    }

    public String getTxid() {
        return txid;
    }

    public String getPeer() {
        return peer;
    }

    public int getSuccess() {
        return success;
    }
}
