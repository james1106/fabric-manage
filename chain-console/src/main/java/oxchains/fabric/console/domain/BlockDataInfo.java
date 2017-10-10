package oxchains.fabric.console.domain;

/**
 * @author aiet
 */
public class BlockDataInfo {

    private String txid;
    private int type;
    private int version;
    private long epoch;
    private String channel;
    private String nonce;
    private String signature;

    public BlockDataInfo(){
    }

    public BlockDataInfo createdBy(String signature, String nonce) {
        this.nonce = nonce;
        this.signature = signature;
        return this;
    }

    public BlockDataInfo channel(String channel, long epoch, int version, int type){
        this.type = type;
        this.version = version;
        this.epoch = epoch;
        this.channel = channel;
        return this;
    }

    public BlockDataInfo ofTx(String txid) {
        this.txid = txid;
        return this;
    }

    public String getTxid() {
        return txid;
    }

    public int getType() {
        return type;
    }

    public int getVersion() {
        return version;
    }

    public long getEpoch() {
        return epoch;
    }

    public String getChannel() {
        return channel;
    }

    public String getNonce() {
        return nonce;
    }

    public String getSignature() {
        return signature;
    }

}
