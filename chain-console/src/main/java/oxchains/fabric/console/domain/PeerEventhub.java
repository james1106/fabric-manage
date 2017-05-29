package oxchains.fabric.console.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author aiet
 */
@Entity
public class PeerEventhub {

    @Id private String id;
    private String endpoint;
    private String eventhub;
    private String password;
    private Date createtime = new Date();

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonSetter
    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
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

    public String getEventhub() {
        return eventhub;
    }

    public void setEventhub(String eventhub) {
        this.eventhub = eventhub;
    }

    @Override
    public String toString() {
        return String.format("%s@%s, listening on %s", id, endpoint, eventhub);
    }

    @Override
    public boolean equals(Object another) {
        if (another == null) return false;
        if (another instanceof PeerEventhub && this.id != null) {
            PeerEventhub anotherInstance = (PeerEventhub) another;
            return anotherInstance.id != null && this.id.equals(anotherInstance.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
