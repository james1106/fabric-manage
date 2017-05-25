package oxchains.fabric.console.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import oxchains.fabric.console.rest.common.RestResp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author aiet
 */
public class AuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(SC_FORBIDDEN);
        response.setContentType(APPLICATION_JSON_VALUE);

        String message = "authentication error: ";
        if (authException.getCause() != null) {
            message += authException
              .getCause()
              .getMessage();
        } else {
            message += authException.getMessage();
        }
        byte[] body = new ObjectMapper().writeValueAsBytes(RestResp.fail(message));
        response
          .getOutputStream()
          .write(body);
    }

}
