package cn.shuangbofu.rhea.common.tuple;

/**
 * Created by shuangbofu on 2020-03-17 10:17
 */
public class ThreeTuple<A, B, C> extends TwoTuple<A, B> {

    public final C third;

    public ThreeTuple(A a, B b, C c) {
        super(a, b);
        third = c;
    }

    @Override
    public String toString() {
        return "(" + first + "," + second + "," + third + ")";
    }

}
