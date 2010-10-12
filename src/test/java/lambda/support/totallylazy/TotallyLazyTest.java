package lambda.support.totallylazy;

import static com.googlecode.totallylazy.Sequences.*;
import static lambda.Parameters.*;
import static lambda.support.totallylazy.LambdaTotallyLazy.*;
import static lambda.support.totallylazy.User.*;
import static org.junit.Assert.*;
import static lambda.Lambda.*;

import lambda.Fn1;
import lambda.annotation.LambdaParameter;

import org.junit.Test;

import com.googlecode.totallylazy.Callable1;
import com.googlecode.totallylazy.Callable2;
import com.googlecode.totallylazy.Sequence;

public class TotallyLazyTest {
    @Test
    public void convertCallable1ToFn1() throws Exception {
        Fn1<String, String> fn = toFn(callable(s, s.toUpperCase()));
        assertEquals("HELLO", fn.call("hello"));
    }

    @Test
    public void convertFn1ToCallable1() throws Exception {
        Callable1<String, String> fn = toCallable(λ(s, s.toUpperCase()));
        assertEquals("HELLO", fn.call("hello"));
    }

    @Test
    public void convertFn2ToCallable2() throws Exception {
        Callable2<String, String, String> fn = toCallable(λ(s, t, s.toUpperCase() + " " + t.toUpperCase()));
        assertEquals("HELLO WORLD", fn.call("hello", "world"));
    }

    @Test
    public void convertCallable2ToFn2() {
        Callable2<Integer, Integer, Integer> times = callable(n, m, n * m);
        assertEquals(6, (int) toFn(times).call(3, 2));
    }

    // Adapted from com.googlecode.totallylazy.proxy.CallTest
    @LambdaParameter
    static User user;
    
    @Test
    public void canSortByProxy() throws Exception {
        User matt = user("Matt", "Savage");
        User dan = user("Dan", "Bodart");
        User bob = user("Bob", "Marshal");
        Sequence<User> unsorted = sequence(matt, dan, bob);
        Sequence<User> sorted = unsorted.sortBy(callable(user, user.firstName()));
        assertEquals(sequence(bob, dan, matt).toList(), sorted.toList());
  }

    @Test
    public void canMapAMethod() throws Exception {
        Sequence<User> users = sequence(user("Dan", "Bodart"), user("Matt", "Savage"));
        Sequence<String> firstNames = users.map(callable(user, user.firstName()));
        assertEquals(sequence("Dan", "Matt").toList(), firstNames.toList());
    }

    @Test
    public void canMapAMethodWithAnArgument() throws Exception {
        Sequence<User> users = sequence(user("Dan", "Bodart"), user("Matt", "Savage"));
        Sequence<String> firstNames = users.map(callable(user, user.say("Hello")));
        assertEquals(sequence("Dan says 'Hello'", "Matt says 'Hello'").toList(), firstNames.toList());
    }
}
