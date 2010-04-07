package lambda.enumerable.collection;

import java.util.HashMap;
import java.util.Map;

import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.Enumerable;

public class EMap<K, V> implements Map<K, V> {
    private final Map<K, V> map;

    public EMap() {
        this(new HashMap<K, V>());
    }

    public EMap(Map<K, V> map) {
        this.map = map;
    }
    
    public Map<K, V> delegate() {
        return map;
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
        return Enumerable.each(this, block);
    }

    public <R> EMap<K, V> eachKey(Fn1<K, R> block) {
        return Enumerable.eachKey(this, block);
    }

    public <R> EMap<K, V> eachValue(Fn1<V, R> block) {
        return Enumerable.eachValue(this, block);
    }

    public EList<java.util.Map.Entry<K, V>> select(Fn2<K, V, Boolean> block) {
        return Enumerable.select(this, block);
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
}
