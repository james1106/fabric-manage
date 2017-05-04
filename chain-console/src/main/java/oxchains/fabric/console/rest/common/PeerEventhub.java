package oxchains.fabric.console.rest.common;

/**
 * @author aiet
 */
public class PeerEventhub {

    private String id;
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
}
