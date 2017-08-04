package oxchains.fabric.console.auth;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.remoting.p2p.Group;
import com.alibaba.dubbo.rpc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import oxchains.fabric.console.auth.JwtService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by root on 17-8-3.
 */
@Activate(group = {Constants.PROVIDER})
public class ProviderFilter implements Filter {
    private JwtService jwtService;

    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String token = RpcContext.getContext().getAttachment("authentication");
        System.out.println("===ProviderFilter===token==="+token);
       if (!StringUtils.isEmpty(token)) {
            jwtService.parse(token)
                    .ifPresent(jwtAuthentication -> SecurityContextHolder
                            .getContext()
                            .setAuthentication(jwtAuthentication));
        }
        return invoker.invoke(invocation);
    }
}
