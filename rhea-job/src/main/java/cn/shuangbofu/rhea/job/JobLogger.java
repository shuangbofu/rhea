package cn.shuangbofu.rhea.job;

import cn.shuangbofu.rhea.common.LogData;
import cn.shuangbofu.rhea.common.utils.FileUtil;

import java.util.List;

/**
 * Created by shuangbofu on 2020/10/18 下午7:57
 * <p>
 * 日志接口
 */
public interface JobLogger {

    String getKey();

    void info(String s, Object... args);

    void info(String s, Throwable t, Object... args);

    void error(String s, Throwable t, Object... args);

    void error(String s, Object... args);

    LogData getLog(int offset, int length);

    List<FileUtil.LogResult> close();

    boolean closed();
}
