package cn.shuangbofu.rhea.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by shuangbofu on 2020/10/18 下午5:57
 */
public class DefaultThreadFactory implements ThreadFactory {
    private final String namePrefix;
    private final AtomicLong count = new AtomicLong(1);

    public DefaultThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(namePrefix + count.getAndIncrement());
        return thread;
    }
}
