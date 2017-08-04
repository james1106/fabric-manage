package oxchains.fabric.console.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by root on 17-8-4.
 */
public class JwtAuth implements Serializable{
    private String username;

    private Date expiredate;

    public JwtAuth(){

    }

    public JwtAuth(String username,Date  expiredate){
        this.username = username;
        this.expiredate = expiredate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getExpiredate() {
        return expiredate;
    }

    public void setExpiredate(Date expiredate) {
        this.expiredate = expiredate;
    }
}
