package oxchains.fabric.console.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

/**
 * @author aiet
 */
@Entity
public class UserToken {

    @OneToOne
    @MapsId
    private User user;

    @Id private String token;

    public UserToken() {
    }

    public UserToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public String getUsername() {
        return this.user.getUsername();
    }

    public String getToken() {
        return token;
    }

}
