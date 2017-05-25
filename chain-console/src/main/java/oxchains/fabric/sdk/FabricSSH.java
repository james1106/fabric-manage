package oxchains.fabric.sdk;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * @author aiet
 */
@Component
public class FabricSSH {

    private SSHClient sshClient;

    private static final Logger LOG = LoggerFactory.getLogger(FabricSSH.class);

    @Value("${ssh.host}") private String host;
    @Value("${ssh.port}") private int port;
    @Value("${ssh.username}") private String username;
    @Value("${ssh.password}") private String password;

//    @PostConstruct
    private void init() {
        try {
            sshClient = new SSHClient();
            sshClient.loadKnownHosts();
            sshClient.connect(host, port);
            sshClient.authPassword(username, password);

        } catch (Exception e) {
            LOG.error("failed to init ssh client to {}", host, e);
        }
    }

    public Optional<SSHResponse> startPeer(String peerId) {
        String commandline = String.format("env PEER_CFG_PATH=/home/aiet/Dev/fabric-test/peers/%s PATH=/home/aiet/Dev/go/src/github.com/hyperledger/fabric/build/bin:$PATH nohup peer node start --peer-defaultchain=false >> %s.log 2>&1 &", peerId, peerId);
        try (Session session = sshClient.startSession()) {
            final Command command = session.exec(commandline);
            command.join();
            return Optional.of(new SSHResponse(command));
        } catch (Exception e) {
            LOG.error("failed to start peer {}", peerId, e);
        }
        return empty();
    }

    public Optional<SSHResponse> stopPeer(String peerId) {
        String commandline = String.format("env PEER_CFG_PATH=/home/aiet/Dev/fabric-test/peers/%s PATH=/home/aiet/Dev/go/src/github.com/hyperledger/fabric/build/bin:$PATH peer node stop", peerId);
        try (Session session = sshClient.startSession()) {
            final Command command = session.exec(commandline);
            command.join();
            return Optional.of(new SSHResponse(command));
        } catch (Exception e) {
            LOG.error("failed to stop peer {}", peerId, e);
        }
        return empty();
    }

    @PreDestroy
    public void destroy() {
        try {
            if (sshClient != null && sshClient.isConnected()) {
                sshClient.disconnect();
            }
        } catch (Exception e) {
            LOG.error("failed to shutdown ssh", e);
        }
    }

    public static class SSHResponse {
        private final int exitStatus;
        private String error;
        private final String exitMessage;

        private SSHResponse(Command command) {
            exitMessage = command.getExitErrorMessage();
            exitStatus = command.getExitStatus();
            if (exitStatus != 0) {
                try {
                    error = IOUtils.toString(command.getErrorStream());
                } catch (Exception e) {
                    LOG.error("failed to get ssh command error stream ", e);
                }
            }
        }

        public boolean succeeded() {
            return exitStatus == 0;
        }

        public String error() {
            return error;
        }
    }

}
