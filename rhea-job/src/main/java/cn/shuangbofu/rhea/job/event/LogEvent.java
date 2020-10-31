package cn.shuangbofu.rhea.job.event;

import cn.shuangbofu.rhea.common.utils.FileUtil;
import cn.shuangbofu.rhea.job.JobLogger;
import lombok.Data;

import java.util.List;

/**
 * Created by shuangbofu on 2020/10/30 18:38
 */
@Data
public class LogEvent implements Event {
    private String key;
    private List<FileUtil.LogResult> logs;
    private JobLogger logger;

    public LogEvent(String key, List<FileUtil.LogResult> logs) {
        this.key = key;
        this.logs = logs;
    }

    public LogEvent(JobLogger logger) {
        this.logger = logger;
        key = logger.getKey();
    }
}
