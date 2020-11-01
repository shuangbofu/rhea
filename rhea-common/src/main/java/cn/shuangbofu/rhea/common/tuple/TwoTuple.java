package cn.shuangbofu.rhea.common.tuple;

import lombok.EqualsAndHashCode;

/**
 * Created by shuangbofu on 2020-03-17 10:17
 */
@EqualsAndHashCode
public class TwoTuple<A, B> {

    public final A first;

    public final B second;

    public TwoTuple(A a, B b) {
        first = a;
        second = b;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
