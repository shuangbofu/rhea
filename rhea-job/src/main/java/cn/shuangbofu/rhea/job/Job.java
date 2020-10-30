package cn.shuangbofu.rhea.job;

/**
 * Created by shuangbofu on 2020/10/18 下午5:24
 */
public interface Job extends Runnable {
    void cancel();

    Executor executor();
}
