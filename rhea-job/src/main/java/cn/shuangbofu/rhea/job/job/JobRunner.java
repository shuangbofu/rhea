package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.common.utils.FileUtil;
import cn.shuangbofu.rhea.job.JobLogger;
import cn.shuangbofu.rhea.job.conf.params.Param;
import cn.shuangbofu.rhea.job.conf.params.ParamStore;
import cn.shuangbofu.rhea.job.event.ActionUpdateEvent;
import cn.shuangbofu.rhea.job.event.EventHandler;
import cn.shuangbofu.rhea.job.event.LogEvent;
import cn.shuangbofu.rhea.job.utils.YarnUtil;
import lombok.Getter;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by shuangbofu on 2020/10/30 上午11:17
 */
public class JobRunner extends EventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);
    @Getter
    private final FlinkJob flinkJob;
    private final Object lock = new Object();
    private final ParamStore paramStore;
    private Restart restart;
    private RemoteExecutor executor;
    private JobLogger logger;

    public JobRunner(FlinkJob flinkJob, List<Param> params) {
        this.flinkJob = flinkJob;
        paramStore = new ParamStore();
        setParams(params);
    }

    public void setParams(List<Param> params) {
        paramStore.addParams(params);
        executor = new RemoteExecutor(paramStore, logger);
    }

    public JobRunner setupLogger(String command) {
        // 创建logger
        String logName = String.format("%s_%s_%s", flinkJob.getActionId(), System.currentTimeMillis(), command);
        logger = new FileLogger(logName, flinkJob.getJobName(), false);
        return this;
    }

    public void restart() {

    }

    public void run() {
        synchronized (lock) {
            setExecuting(JobStatus.RUNNING);
        }
    }

    public void submit() {
        synchronized (lock) {
            setExecuting(JobStatus.SUBMITTED);

        }
    }

    public void publish() {
        synchronized (lock) {
            setExecuting(JobStatus.PUBLISHED);


        }
    }

    public void stop() {
        try {
            YarnUtil.kill(paramStore.getValue("rsAddress"), flinkJob.getResult().getApplicationId());
        } catch (IOException | YarnException e) {
            String msg = "kill flink app error";
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public void kill() {
        executor.cancel();
    }

    private boolean isRestart() {
        return restart != null;
    }

    public boolean isRunning() {
        return JobStatus.RUNNING.equals(getStatus());
    }

    public boolean isExecuting() {
        return JobStatus.EXECUTING.equals(getStatus());
    }

    private void setExecuting(JobStatus status) {
        logger.info("任务开始" + status);
        flinkJob.updateStatus(status);
        flinkJob.getResult().setExecuteStatus(status);
        fireEventListeners(new ActionUpdateEvent(flinkJob.getActionId(), flinkJob.getResult(), status));
    }

    private JobStatus getStatus() {
        return flinkJob.getJobStatus();
    }

    private void updateStatus(JobStatus status) {
        flinkJob.updateStatus(status);
        fireEventListeners(new ActionUpdateEvent(flinkJob.getActionId(), status));
    }

    protected void execute() {

    }

    public void closeLogger() {
        if (logger != null) {
            List<FileUtil.LogResult> logResults = logger.close();
            fireEventListeners(new LogEvent(logger.getKey(), logResults));
        }
    }

    public RemoteExecutor getExecutor() {
        return executor;
    }

    static class Restart {

    }
}
