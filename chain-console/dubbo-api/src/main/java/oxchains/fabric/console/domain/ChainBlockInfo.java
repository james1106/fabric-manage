package oxchains.fabric.console.domain;

import com.google.protobuf.ByteString;
import org.hyperledger.fabric.protos.common.Common.*;
import org.hyperledger.fabric.sdk.BlockInfo;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.bouncycastle.util.encoders.Hex.toHexString;

/**
 * @author aiet
 */
public class ChainBlockInfo {

    private int size;
    private String previous;
    private String hash;
    private List<BlockDataInfo> datalist = emptyList();
    private long number;

    public ChainBlockInfo() {}

    public ChainBlockInfo(BlockInfo blockInfo) {
        this.number = blockInfo.getBlockNumber();
        Block block = blockInfo.getBlock();
        if (block.hasHeader()) {
            BlockHeader blockHeader = block.getHeader();
            this.hash = toHexString(blockHeader
              .getDataHash()
              .toByteArray());
            this.previous = toHexString(blockHeader
              .getPreviousHash().toByteArray());
        }
        if (block.hasData()) {
            BlockData blockData = block.getData();
            this.size = blockData.getDataCount();
            if(this.size>0) this.datalist = new ArrayList<>(size);
            for (ByteString data : blockData.getDataList()) {
                try {
                    Envelope envelope = Envelope.parseFrom(data);
                    Header header = Payload.parseFrom(envelope.getPayload()).getHeader();
                    SignatureHeader signatureHeader = SignatureHeader.parseFrom(header.getSignatureHeader());
                    ChannelHeader channelHeader = ChannelHeader.parseFrom(header.getChannelHeader());

                    BlockDataInfo blockDataInfo = new BlockDataInfo().createdBy(
                      toHexString(envelope.getSignature().toByteArray()),
                      toHexString(signatureHeader.getNonce().toByteArray())
                    ).channel(
                      channelHeader.getChannelId(),
                      channelHeader.getEpoch(),
                      channelHeader.getVersion(),
                      channelHeader.getType()
                    ).ofTx(channelHeader.getTxId());

                    this.datalist.add(blockDataInfo);

                } catch (Exception ignored) {}
            }
        }
    }

    public long getNumber() {
        return number;
    }

    public int getSize() {
        return size;
    }

    public String getPrevious() {
        return previous;
    }

    public String getHash() {
        return hash;
    }

    public List<BlockDataInfo> getDatalist() {
        return datalist;
    }
}
