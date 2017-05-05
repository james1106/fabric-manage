package oxchains.fabric.console.domain;

import org.hyperledger.fabric.sdk.EventHub;

import java.util.Properties;

/**
 * @author aiet
 */
public class EventHubInfo {

    private final long online;
    private final long offline;
    private final int connected;
    private final String id;
    private final String endpoint;
    private final long lastattempt;

    public EventHubInfo(EventHub eventHub){
        this.online = eventHub.getConnectedTime();
        this.offline = eventHub.getDisconnectedTime();
        this.connected = eventHub.isConnected()?1:0;
        this.id = eventHub.getName();
        this.endpoint = eventHub.getUrl();
        this.lastattempt = eventHub.getLastConnectedAttempt();
    }

    public long getOnline() {
        return online;
    }

    public long getOffline() {
        return offline;
    }

    public int getConnected() {
        return connected;
    }

    public String getId() {
        return id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public long getLastattempt() {
        return lastattempt;
    }

}
