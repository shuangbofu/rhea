package cn.shuangbofu.rhea.job.job.shell;

/**
 * Created by shuangbofu on 2020/10/30 18:06
 */
public interface IProcess {

    void execute();

    void kill();

    int getProcessId();
}
