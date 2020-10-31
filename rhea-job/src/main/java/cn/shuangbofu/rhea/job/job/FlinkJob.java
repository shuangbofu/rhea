package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.job.conf.JobActionProcess;
import cn.shuangbofu.rhea.job.conf.JobConf;
import cn.shuangbofu.rhea.job.conf.JobText;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/18 下午5:35
 *
 * @author shuangbofu
 */
@Getter
@Accessors(chain = true)
public abstract class FlinkJob {

    protected final String jobName;
    protected final Long actionId;
    protected final JobText text;
    protected final JobConf conf;
    protected final JobActionProcess result;
    private final Long jobId;
    private final Object lock = new Object();
    protected JobStatus jobStatus;
    protected JobRunner runner;

    public FlinkJob(Long jobId, String jobName, Long actionId, JobStatus jobStatus, JobText text, JobConf conf, JobActionProcess result) {
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

    public void publish() {
        runner.logger().info("创建checkpoint文件夹");
//        String hdfsAddress = runner.getParamStore().getValue("hdfsAddress");
//        String cmd = String.format("hdfs dfs -mkdir -p %s/%s/checkpoint", hdfsAddress + "/flink", jobName);
//        runner.getExecutor().ssh(cmd, false);
        runner.logger().info("成功!");

    }

    public void submit() {
        runner.logger().info("成功!");
    }

    public void run() {
        runner.logger().info("成功!");
    }

    protected String getRootPath() {
        return "/home/" + runner.getParamStore().getValue("username") + "/flinkJob";
    }
}
