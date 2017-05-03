package oxchains.fabric.console.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author aiet
 */
@Entity
public class UserToken {

    @Id private Long id;

    private String token;
    private String username;

    public UserToken() {
    }

    public UserToken(User user, String token) {
        this.username = user.getUsername();
        this.id = user.getId();
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

}
