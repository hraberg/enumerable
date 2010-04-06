package lambda;

@SuppressWarnings("serial")
public abstract class Fn1IToI extends Fn1<Integer, Integer> {
    public abstract int call(int a1);

    public Integer call(Integer a1) {
        return call(a1.intValue());
    }
}
