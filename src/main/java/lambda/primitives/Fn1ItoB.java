package lambda.primitives;


@SuppressWarnings("serial")
public abstract class Fn1ItoB extends Fn1ItoX<Boolean> {
    public abstract boolean call(int a1);
    
    public Boolean call(Integer a1) {
        return call(a1.intValue());
    }
}
