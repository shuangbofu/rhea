package cn.shuangbofu.rhea.job.event;

import cn.shuangbofu.rhea.common.utils.FileUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Created by shuangbofu on 2020/10/30 18:38
 */
@Data
@AllArgsConstructor
public class LogEvent implements Event {
    private String key;
    private List<FileUtil.LogResult> logs;
}
