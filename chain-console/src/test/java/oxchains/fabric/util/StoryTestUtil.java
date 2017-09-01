package oxchains.fabric.util;

import oxchains.fabric.console.domain.ChainCodeInfo;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

/**
 * @author aiet
 */
public final class StoryTestUtil {

    public static String propertyParse(String key, Map<?, String> properties){
        if(key.startsWith("#")){
            return properties.get(key.substring(1));
        }
        return key;
    }

    public static String scriptParse(String arg) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        Pattern pattern = Pattern.compile("(?<=\\{%)(.*?)(?=%\\})");
        return Arrays
          .stream(arg.split(" "))
          .map(s -> {
              Matcher matcher = pattern.matcher(s);
              try {
                  return matcher.find() ? new BigDecimal(engine
                    .eval(matcher.group())
                    .toString()).toPlainString() : s;
              } catch (ScriptException e) {
                  e.printStackTrace();
              }
              return s;
          })
          .collect(joining(" "));
    }
}
