package oxchains.fabric.console.domain;

import org.hyperledger.fabric.protos.common.Common.*;
import org.hyperledger.fabric.sdk.TransactionInfo;
import oxchains.fabric.console.rest.common.KV;

import static org.bouncycastle.util.encoders.Hex.toHexString;

/**
 * @author aiet
 */
public class TxInfo {

    private BlockDataInfo blockdata;
    private String signature;
    private int validation;
    private String txid;

    public TxInfo() {
    }

    public TxInfo(TransactionInfo transactionInfo) {
        Envelope envelope = transactionInfo.getEnvelope();
        this.txid = transactionInfo.getTransactionID();
        this.validation = transactionInfo
          .getValidationCode()
          .getNumber();
        this.signature = envelope
          .getSignature()
          .toStringUtf8();
        try {
            Payload payload = Payload.parseFrom(envelope.getPayload());
            Header header = payload.getHeader();
            ChannelHeader channelHeader = ChannelHeader.parseFrom(header.getChannelHeader());
            SignatureHeader signatureHeader = SignatureHeader.parseFrom(header.getSignatureHeader());
            this.blockdata = new BlockDataInfo()
              .createdBy(toHexString(envelope
                .getSignature()
                .toByteArray()), toHexString(signatureHeader
                .getNonce()
                .toByteArray()))
              .channel(channelHeader.getChannelId(), channelHeader.getEpoch(), channelHeader.getVersion(), channelHeader.getType())
              .ofTx(channelHeader.getTxId());
        } catch (Exception ignored) {
        }

    }

    public BlockDataInfo getBlockdata() {
        return blockdata;
    }

    public String getSignature() {
        return signature;
    }

    public int getValidation() {
        return validation;
    }

    public String getTxid() {
        return txid;
    }
}
