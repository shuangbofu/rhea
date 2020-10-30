package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.job.conf.JobActionResult;
import cn.shuangbofu.rhea.job.conf.JobConf;
import cn.shuangbofu.rhea.job.conf.JobText;
import lombok.Getter;

/**
 * Created by shuangbofu on 2020/10/18 下午5:35
 *
 * @author shuangbofu
 */
@Getter
public abstract class FlinkJob {

    private final Long jobId;
    private final String jobName;
    private final Long actionId;
    private final JobText text;
    private final JobConf conf;
    private final Object lock = new Object();
    private final JobActionResult result;
    private JobStatus jobStatus;
    private JobRunner runner;

    public FlinkJob(Long jobId, String jobName, Long actionId, JobStatus jobStatus, JobText text, JobConf conf, JobActionResult result) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.actionId = actionId;
        this.jobStatus = jobStatus;
        this.text = text;
        this.conf = conf;
        this.result = result;
    }

    public Long getJobId() {
        return jobId;
    }

    public void launching() {

    }

    public void setRunner(JobRunner runner) {
        this.runner = runner;
    }

    public void updateStatus(JobStatus status) {
        synchronized (lock) {
            jobStatus = status;
        }
    }

    public JobStatus getJobStatus() {
        synchronized (lock) {
            return jobStatus;
        }
    }
}
