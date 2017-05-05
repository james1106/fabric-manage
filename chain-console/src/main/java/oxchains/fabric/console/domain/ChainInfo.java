package oxchains.fabric.console.domain;

import org.hyperledger.fabric.sdk.BlockchainInfo;

import static org.bouncycastle.util.encoders.Hex.toHexString;

/**
 * @author aiet
 */
public class ChainInfo {

    public ChainInfo() {
    }

    private String hash;
    private String next;
    private long height;
    private int init;

    public ChainInfo(BlockchainInfo blockchainInfo) {
        this.hash = toHexString(blockchainInfo.getCurrentBlockHash());
        this.next = toHexString(blockchainInfo.getPreviousBlockHash());
        this.height = blockchainInfo.getHeight();
        this.init = blockchainInfo
          .getBlockchainInfo()
          .isInitialized() ? 1 : 0;
    }

    public String getHash() {
        return hash;
    }

    public String getNext() {
        return next;
    }

    public long getHeight() {
        return height;
    }

    public int getInit() {
        return init;
    }
}
