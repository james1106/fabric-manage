package oxchains.fabric.console.rest.common;

import org.hyperledger.fabric.sdk.ProposalResponse;

/**
 * @author aiet
 */
public class QueryResult {
    private String payload;
    private String peer;
    private String txid;

    public QueryResult(ProposalResponse response) {
        this.txid = response.getTransactionID();
        this.payload = response
          .getProposalResponse()
          .getResponse()
          .getPayload()
          .toStringUtf8();
        this.peer = response
          .getPeer()
          .getName();
    }

    public String getPeer() {
        return peer;
    }

    public String getTxid() {
        return txid;
    }

    public String getPayload() {
        return payload;
    }
}
