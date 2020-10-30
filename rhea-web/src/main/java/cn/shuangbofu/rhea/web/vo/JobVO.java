package cn.shuangbofu.rhea.web.vo;

import cn.shuangbofu.rhea.common.enums.JobType;
import cn.shuangbofu.rhea.job.alarm.AlarmConfig;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/18 上午11:03
 */
@Data
@Accessors(chain = true)
public class JobVO extends BaseUserVO<JobVO> {
    private Long id;
    private String jobName;
    private JobType jobType;
    private String jobDesc;
    private AlarmConfig alarmConfig;
}
