package cn.shuangbofu.rhea.job.executor;

/**
 * Created by shuangbofu on 2020/10/18 下午5:29
 * 执行任务/命令
 * <p>
 * 包含了集群和组件相关的使用和配置
 */
public interface Executor {

    void ssh(String cmd, boolean all);

    void scp(String localPath, String remotePath, boolean all);

    void cancel();
}
