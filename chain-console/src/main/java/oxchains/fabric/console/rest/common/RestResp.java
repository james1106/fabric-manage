package oxchains.fabric.console.rest.common;

import io.netty.util.internal.StringUtil;

import java.util.Optional;

/**
 * @author aiet
 */
public class RestResp {
    public static ThreadLocal<String> threadLocal = new ThreadLocal<>();
    public final int status;
    public final String message;
    public final Object data;

    private RestResp(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    private RestResp(int status, String messsage) {
        this(status, messsage, null);
    }

    public static RestResp success(Object data) {
        return new RestResp(1, "success", data);
    }

    public static RestResp success(String message, Object data){
        return new RestResp(1, message, data);
    }

    public static RestResp fail(String message, Object data){
        return new RestResp(-1, message, data);
    }

    public static RestResp fail(String message){
        if(StringUtil.isNullOrEmpty(message)){
            message = "fail";
        }
        return new RestResp(-1, message);
    }

    public static RestResp fail(){
        return new RestResp(-1, "fail");
    }
    public static Object set(String message){ if(StringUtil.isNullOrEmpty(threadLocal.get()))threadLocal.set(message); return null;};
    public static Object set(String message,boolean s){ if(s) threadLocal.set(message);else{set(message);}; return null;};
    public static String get(){ String message = threadLocal.get(); threadLocal.remove(); return message;};



}
