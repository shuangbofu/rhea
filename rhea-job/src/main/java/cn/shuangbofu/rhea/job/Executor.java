package cn.shuangbofu.rhea.job;

import java.util.Map;

/**
 * Created by shuangbofu on 2020/10/18 下午5:29
 * 执行任务/命令
 * <p>
 * 包含了集群和组件相关的使用和配置
 */
public interface Executor {

    void submit(cn.shuangbofu.rhea.job.Job job);

    boolean cancel();

    Map<String, Object> getProperties();
}
