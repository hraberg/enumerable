package lambda;

import static java.util.Arrays.*;
import static lambda.Lambda.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lambda.annotation.LambdaParameter;

import clojure.lang.IPersistentMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentTreeMap;

public class DataSpike {
    static class Work {
        String company, role;
    }

    static class Address {
        String street;
        int number;
    }

    static class Person {
        String name;
        Work work;
        int age;
        List<String> languages;
    }
    
    Person p = new Person() {{ name = "Jon"; age = 32; }};

    @SuppressWarnings("unchecked")
    public static class HashSpike {
        @SuppressWarnings({ "serial", "rawtypes" })
        public static void main(String... args) {

            new HashMap() {
                {
                    put("name", "Hakan");
                    put("age", 34);
                    put("address", new HashMap() {
                        {
                            put("street", "");
                            put("number", 239);
                        }
                    });
                }
            };
            
            Person person = new Person() {{ name = "Hakan"; age = 34; work = new Work() {{ company = "UBS"; role = "coder"; }}; languages = asList("Java", "Ruby"); }};
            Hash hash = _(name = "Hakan", age = 34, work = _(company = "UBS", role = "coder"), languages = asList("Java", "Ruby"));
            
            assert 34 == hash._(age);
            assert "Hakan" == hash._(name);
            assert hash.merge(address = _(street = "New North Road", number = 239)).equals(
                    _(name = "Hakan", age = 34, address = _(street = "New North Road", number = 239),
                            work = Hash(company = "UBS", role = "coder")));
            assert hash.equals(hash.map);
        
            assert 34 == person.age;
            assert "Hakan" == person.name;
            assert hash.merge(address = _(street = "New North Road", number = 239)).equals(
                    _(name = "Hakan", age = 34, address = _(street = "New North Road", number = 239),
                            work = Hash(company = "UBS", role = "coder")));
            assert hash.equals(hash.map);
            
            
            fib(42);

//            fib :: Integer -> Integer
//            fib 0 = 0
//            fib 1 = 1
//            fib n = fib (n-1) + fib (n-2)
        }
        
        
        public static Integer fib(Integer n) {
           return m(λ(n == 0),      λ(0), 
                    λ(n == 1),      λ(1), 
                    λ((Integer) n), λ(fib(n - 1) + fib(n - 2)));
            
//            
//            return match(n,
//                        0, 0,
//                        1, 1,
//                        N, fib(N - 1) + fib(N - 2));
//            return match(n,
//                         0, 0,
//                         1, 1,
//                         N, fib(N - 1) + fib(N - 2));
//            return match(0, 0,
//                         1, 1,
//                         N, fib(N - 1) + fib(N - 2));
        }
        

        // Similar to @LambdaParameter, use static fields to get typed symbols.
        // Macro expansion will convert:
        // - field access into its name, MyClass.symbol -> "symbol"
        // - assignment into name and value, Myclass.symbol = 42 ->
        // Map.Entry("symbol", 42)
        @Target(ElementType.FIELD)
        @interface Symbol {
        }

        static @Symbol
        String role;
        static @Symbol
        String company;
        static @Symbol
        String street;
        static @Symbol
        String name;
        static @Symbol
        Hash work;
        static @Symbol
        Integer age;
        static @Symbol
        Integer number;
        static @Symbol
        Hash address;
        @Symbol
        static List<String> languages;

        @Target(ElementType.FIELD)
        @interface Match {
        }
        @Match
        static Integer N;

        public static <T> T match(Object... claueses) {
            return null;
        }

        public static <T> T m(Fn0<?>... claueses) {
            return null;
        }

        public static Hash Hash(Object... keyValues) {
            return new Hash(keyValues);
        }

        public static Hash _(Object... keyValues) {
            return new Hash(keyValues);
        }

        public static List<Object> path(Object... keys) {
            return asList(keys);
        }

        public static class Hash {
            public final Map<String, Object> map = new HashMap<String, Object>();

            public Hash(Object... entries) {
                for (Object entry : entries) {
                    Map.Entry<String, Object> e = (Entry<String, Object>) entry;
                    map.put(e.getKey(), e.getValue());
                }
            }

            public Hash selectKeys(Object... keys) {
                Hash hash = new Hash();
                for (Object key : keys) {
                    hash.map.put((String) key, hash.get(key));
                }
                return hash;
            }

            public <K> K get(K key) {
                return (K) map.get((String) key);
            }

            public <K> K _(K key) {
                return get(key);
            }

            public Hash merge(Object... keyValuePairs) {
                Hash hash = new Hash();
                hash.map.putAll(map);
                hash.map.putAll(Hash(keyValuePairs).map);
                return hash;
            }

            public int hashCode() {
                return map.hashCode();
            }

            public boolean equals(Object obj) {
                return map.equals(obj);
            }

            public String toString() {
                return map.toString();
            }
        }
    }
}
