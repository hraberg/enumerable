package lambda;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lambda.enumerable.collection.EList;
import static java.util.Arrays.*;

import static lambda.enumerable.Enumerable.*;

public class TestBase {
    public EList<Integer> oneToTen = toList(list(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    public EList<Integer> oneToFive = oneToTen.subList(0, 5);
    public HashMap<String, Integer> stringsToInts = new HashMap<String, Integer>();

    {
        stringsToInts.put("hello", 1);
        stringsToInts.put("world", 2);
    }

    public <E> List<E> list(E... elements) {
        return new ArrayList<E>(asList(elements));
    }

    public <E> List<E> list(Class<E> type) {
        return new ArrayList<E>();
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
