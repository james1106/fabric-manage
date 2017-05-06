package oxchains.fabric.console.rest.common;

import java.util.Map;

/**
 * @author aiet
 */
public class KV<K, V> implements Map.Entry<K, V> {

    private K k;
    private V v;

    public KV(K k, V v) {
        this.k = k;
        this.v = v;
    }

    @Override
    public K getKey() {
        return k;
    }

    @Override
    public V getValue() {
        return v;
    }

    @Override
    public V setValue(V value) {
        v = value;
        return v;
    }

}
