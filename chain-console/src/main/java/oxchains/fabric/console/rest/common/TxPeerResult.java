package oxchains.fabric.console.rest.common;

/**
 * @author aiet
 */
public class TxPeerResult {

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
