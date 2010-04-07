package lambda.enumerable.collection;

import java.util.HashMap;
import java.util.Map;

import lambda.Fn1;
import lambda.Fn2;
import lambda.enumerable.Enumerable;

public class EHashMap<K, V> extends HashMap<K, V> implements EMap<K, V> {
    private static final long serialVersionUID = 4713034666368384525L;

    public EHashMap() {
    }

    public EHashMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public ESet<Map.Entry<K, V>> entrySet() {
        return new EHashSet<Map.Entry<K, V>>(super.entrySet());
    }

    public ESet<K> keySet() {
        return new EHashSet<K>(super.keySet());
    }

    public ECollection<V> values() {
        return new EArrayList<V>(super.values());
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
}
