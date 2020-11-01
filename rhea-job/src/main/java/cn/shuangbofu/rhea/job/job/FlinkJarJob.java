package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.job.alarm.AlarmConfig;
import cn.shuangbofu.rhea.job.conf.JobActionProcess;
import cn.shuangbofu.rhea.job.conf.JobConf;
import cn.shuangbofu.rhea.job.conf.JobText;

/**
 * Created by shuangbofu on 2020/10/18 下午7:09
 */
public class FlinkJarJob extends FlinkJob {

    public FlinkJarJob(Long jobId, String jobName, Long actionId, JobStatus jobStatus, JobText text, JobConf conf, JobActionProcess result, boolean current, AlarmConfig alarmConfig) {
        super(jobId, jobName, actionId, jobStatus, text, conf, result, current, alarmConfig);
    }

    @Override
    public void publish() {
        super.publish();

    }
}
