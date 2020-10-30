package cn.shuangbofu.rhea.common.tuple;

/**
 * Created by shuangbofu on 2020-03-26 15:43
 */
public class FourTuple<A, B, C, D> extends ThreeTuple<A, B, C> {

    public final D forth;

    public FourTuple(A a, B b, C c, D d) {
        super(a, b, c);
        forth = d;
    }

    @Override
    public String toString() {
        return "(" + first + "," + second + "," + third + "," + forth + ")";
    }
}
