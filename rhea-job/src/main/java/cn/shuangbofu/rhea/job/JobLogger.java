package cn.shuangbofu.rhea.job;

/**
 * Created by shuangbofu on 2020/10/18 下午7:57
 * <p>
 * 日志接口
 */
public interface JobLogger {

    String getKey();

    void info(String s, Object... args);

    void error(String s, Throwable t, Object... args);

    void error(String s, Object... args);

    void close();
}
