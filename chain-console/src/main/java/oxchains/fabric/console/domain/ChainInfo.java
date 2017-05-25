package oxchains.fabric.console.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hyperledger.fabric.sdk.BlockchainInfo;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.bouncycastle.util.encoders.Hex.toHexString;

/**
 * @author aiet
 */
@Entity
public class ChainInfo {

    public ChainInfo() {
    }

    @Transient private String hash;
    @Transient private String next;
    @Transient private long height;

    private String name;
    @ElementCollection(fetch = FetchType.EAGER, targetClass = PeerEventhub.class) private Set<PeerEventhub> peers;
    private String orderer;
    private Date createtime;
    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public ChainInfo(String name, String orderer) {
        this.name = name;
        this.createtime = new Date();
        this.orderer = orderer;
    }

    public ChainInfo(BlockchainInfo blockchainInfo) {
        this.hash = toHexString(blockchainInfo.getCurrentBlockHash());
        this.next = toHexString(blockchainInfo.getPreviousBlockHash());
        this.height = blockchainInfo.getHeight();
    }

    public String getOrderer() {
        return orderer;
    }

    public void setOrderer(String orderer) {
        this.orderer = orderer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<PeerEventhub> getPeers() {
        return peers;
    }

    public void setPeers(Set<PeerEventhub> peers) {
        this.peers = peers;
    }

    public ChainInfo addPeer(PeerEventhub eventhub){
        this.peers.add(eventhub);
        return this;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
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

}
