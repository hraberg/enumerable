package lambda.enumerable.collection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lambda.Fn1;
import lambda.Fn2;

/**
 * A decorator for Map and the actual implementation of the Enumerable module
 * for Maps.
 */
public class EMap<K, V> extends EnumerableModule<Map.Entry<K, V>> implements Map<K, V> {
    protected final Map<K, V> map;

    public EMap() {
        this(new HashMap<K, V>());
    }

    public EMap(Map<K, V> map) {
        this.map = map;
    }

    public ESet<Map.Entry<K, V>> entrySet() {
        return new ESet<Map.Entry<K, V>>(map.entrySet());
    }

    public ESet<K> keySet() {
        return new ESet<K>(map.keySet());
    }

    public ECollection<V> values() {
        return new ECollection<V>(map.values());
    }

    public <R> EMap<K, V> each(Fn2<K, V, R> block) {
        for (Entry<K, V> each : this)
            block.call(each.getKey(), each.getValue());
        return this;
    }

    public <R> EMap<K, V> eachKey(Fn1<K, R> block) {
        for (K each : map.keySet())
            block.call(each);
        return this;
    }

    public <R> EMap<K, V> eachValue(Fn1<V, R> block) {
        for (V each : map.values())
            block.call(each);
        return this;
    }

    public EList<java.util.Map.Entry<K, V>> select(Fn2<K, V, Boolean> block) {
        EList<Map.Entry<K, V>> result = new EList<Map.Entry<K, V>>();
        for (Map.Entry<K, V> each : this)
            if (block.call(each.getKey(), each.getValue()))
                result.add(each);
        return result;
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public V get(Object key) {
        return map.get(key);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public V put(K key, V value) {
        return map.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    public V remove(Object key) {
        return map.remove(key);
    }

    public int size() {
        return map.size();
    }

    public Iterator<java.util.Map.Entry<K, V>> iterator() {
        return map.entrySet().iterator();
    }

    public boolean equals(Object obj) {
        if (obj instanceof EMap<?, ?>)
            return this.map.equals(((EMap<?, ?>) obj).map);
        if (obj instanceof Map<?, ?>)
            return this.map.equals(obj);
        return false;
    }

    public int hashCode() {
        return map.hashCode();
    }

    public String toString() {
        return map.toString();
    }
}
