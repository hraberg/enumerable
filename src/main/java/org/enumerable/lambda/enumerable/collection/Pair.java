package org.enumerable.lambda.enumerable.collection;

import java.util.*;

/**
 * Pair is a simple immutable implementation of the Map.Entry interface.
 */
public class Pair<A, B> implements Map.Entry<A, B> {
    final A first;
    final B second;

    /**
     * Constructs a new pair.
     * 
     * @param first
     *            the first element of the pair.
     * @param second
     *            the second element of the pair.
     * */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /** @return the first element of the pair */
    public A car() {
        return first;
    }

    /** @return the second element of the pair */
    public B cdr() {
        return second;
    }

    /** @return the first element of the pair */
    public A first() {
        return first;
    }

    /** @return the second element of the pair */
    public B second() {
        return second;
    }

    /** @return the first element of the pair */
    public A getKey() {
        return first;
    }

    /** @return the second element of the pair */
    public B getValue() {
        return second;
    }

    /**
     * @param value
     *            this method is only declared to satisfy the Map.Entry
     *            interface.
     * @throws UnsupportedOperationException
     *             when invoked.
     */
    public B setValue(B value) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return a string representation based on the the toString()
     *         implementations of the two elements
     */
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    /**
     * @param o
     *            other element
     * @return true if both elements are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        @SuppressWarnings("rawtypes")
        Pair pair = (Pair) o;

        if (first != null ? !first.equals(pair.first) : pair.first != null)
            return false;
        if (second != null ? !second.equals(pair.second) : pair.second != null)
            return false;

        return true;
    }

    /**
     * 
     * @return a hashCode based on the hashCodes of the two elements.
     */
    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}
