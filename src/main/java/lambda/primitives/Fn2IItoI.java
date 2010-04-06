package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn2IItoI extends Fn2IItoX<Integer> {
    public abstract int call(int a1, int a2);

    public Integer call(Integer a1, Integer a2) {
        return call(a1.intValue(), a2.intValue());
    }
}
