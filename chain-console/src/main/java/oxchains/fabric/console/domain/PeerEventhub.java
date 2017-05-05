package oxchains.fabric.console.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author aiet
 */
@Entity
public class PeerEventhub {

    @Id private String id;
    private String endpoint;
    private String eventhub;

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
    public String toString(){
        return String.format("%s@%s, listening on %s", id, endpoint, eventhub);
    }

}
