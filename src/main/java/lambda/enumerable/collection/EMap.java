package lambda.enumerable.collection;

import java.util.Map;

import lambda.Fn1;
import lambda.Fn2;

public interface EMap<K, V> extends Map<K, V>{
    /**
     * Calls block once for each key in map, passing the key and value to the
     * block as parameters.
     */
    public <R> EMap<K, V> each(Fn2<K, V, R> block);

    /**
     * Calls block once for each value in map, passing the key as parameter.
     */
    public <R> EMap<K, V> eachValue(Fn1<V, R> block);

    
    /**
     * Calls block once for each key in map, passing the key as parameter.
     */
    public <R> EMap<K, V> eachKey(Fn1<K, R> block);

    /**
     * Returns a list containing all Map.Entry pairs for which the block returns
     * true.
     */
    public EList<Map.Entry<K, V>> select(Fn2<K, V, Boolean> block);
}
