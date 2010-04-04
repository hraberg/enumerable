package lambda;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestBase {
    public List<Integer> oneToTen = asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    public List<Integer> oneToFive = oneToTen.subList(0, 5);
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
}
