package org.enumerable.lambda;

import static java.util.Arrays.*;
import static org.enumerable.lambda.enumerable.Enumerable.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.enumerable.lambda.enumerable.collection.EList;
import org.enumerable.lambda.enumerable.collection.EMap;


public class TestBase {
    public EList<Integer> oneToFiveTwice = toList(list(Integer.class));
    public EList<Integer> oneToTen = toList(list(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    public EList<Integer> oneToFive = oneToTen.subList(0, 5);
    public EList<String> animals = toList(list("albatross", "dog", "horse", "fox"));

    public EMap<String, Integer> stringsToInts = new EMap<String, Integer>();

    {
        oneToFiveTwice.addAll(oneToFive);
        oneToFiveTwice.addAll(oneToFive);

        stringsToInts.put("hello", 1);
        stringsToInts.put("world", 2);
    }

    public <E> List<E> list(E... elements) {
        return new ArrayList<E>(asList(elements));
    }

    public <E> List<E> list(Class<E> type) {
        return new ArrayList<E>();
    }

    public <E> List<E> list(Collection<E> collection) {
        return new ArrayList<E>(collection);
    }

    @SuppressWarnings("unchecked")
    public <T extends Object & Serializable> T deserialize(byte[] bytes) throws Exception {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
        try {
            return (T) in.readObject();
        } finally {
            in.close();
        }
    }

    public byte[] serialze(Object original) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        try {
            out.writeObject(original);
            return bos.toByteArray();
        } finally {
            out.close();
        }
    }
}
