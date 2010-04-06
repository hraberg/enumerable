package lambda;

@SuppressWarnings("serial")
public abstract class Fn1BToB extends Fn1<Boolean, Boolean> {
    public abstract boolean call(boolean a1);

    public Boolean call(Boolean a1) {
        return call(a1.booleanValue());
    }
}
