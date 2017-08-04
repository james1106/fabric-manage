package oxchains.fabric.console.rest.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author aiet
 */
@JsonSerialize(using = TxResult.TXResultSerializer.class)
public class TxResult<K, V> extends KV<K, V> implements Serializable{

    private String txid;

    public TxResult(String txid, K k, V v) {
        super(k, v);
        this.txid = txid;
    }

    public String getTxid() {
        return txid;
    }

    public static class TXResultSerializer extends StdSerializer<TxResult> {
        public TXResultSerializer() {
            this(TxResult.class);
        }

        TXResultSerializer(Class<TxResult> t) {
            super(t);
        }

        @Override
        public void serialize(TxResult tx, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("txid", tx.getTxid());
            if (tx.getValue() instanceof Number) {
                gen.writeNumberField(tx
                  .getKey()
                  .toString(), Long.valueOf(tx
                  .getValue()
                  .toString()));
            } else if (tx.getValue() instanceof String) {
                gen.writeStringField(tx
                  .getKey()
                  .toString(), tx
                  .getValue()
                  .toString());
            } else {
                gen.writeObjectField(tx
                  .getKey()
                  .toString(), tx.getValue());
            }
            gen.writeEndObject();
        }
    }

}
