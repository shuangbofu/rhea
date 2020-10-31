package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.common.utils.FileUtil;
import cn.shuangbofu.rhea.job.JobLogger;
import cn.shuangbofu.rhea.job.conf.params.Param;
import cn.shuangbofu.rhea.job.conf.params.ParamStore;
import cn.shuangbofu.rhea.job.event.ActionUpdateEvent;
import cn.shuangbofu.rhea.job.event.EventHandler;
import cn.shuangbofu.rhea.job.event.JobEvent;
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

    public void stop() {
        boolean success = killApp(paramStore.getValue("rsAddress"), flinkJob.getResult().getApplicationId());
        if (!success) {
            throw new RuntimeException("stop job error");
        }
        updateStatus(JobStatus.STOPPED);
        fireEventListeners(new JobEvent(flinkJob.getActionId()));
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

    public ParamStore getParamStore() {
        return paramStore;
    }

    public void execute(String command) {
        synchronized (lock) {
            JobStatus commandStatus = getCommandStatus(command);
            setExecuting(commandStatus);
            try {
                jobExecute(command);
            } catch (Exception e) {
                logger.error("{}异常", command, e);
                updateStatus(JobStatus.ERROR);
                checkAfterError(command);
            }
            updateStatus(commandStatus);
        }
    }

    private void checkAfterError(String command) {
        if (command.equals(Command.RUN)) {
            try {
                String rsAddress = paramStore.getValue("rsAddress");
                List<String> applicationIds = YarnUtil.getApplicationIds(rsAddress, flinkJob.getJobName());
                if (applicationIds.size() > 0) {
                    logger.error("异常后yarn上app:", applicationIds);
                    logger.info("强制kill清除");
                    applicationIds.forEach(applicationId -> killApp(rsAddress, applicationId));
                }
            } catch (YarnException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean killApp(String rsAddress, String applicationId) {
        try {
            YarnUtil.kill(rsAddress, applicationId);
            logger.info("kill {} 成功", applicationId);
            return true;
        } catch (IOException | YarnException e) {
            e.printStackTrace();
            logger.error("kill {} 失败", applicationId);
        }
        return false;
    }

    private JobStatus getCommandStatus(String command) {
        switch (command) {
            case Command.PUBLISH:
                return JobStatus.PUBLISHED;
            case Command.SUBMIT:
                return JobStatus.SUBMITTED;
            case Command.RUN:
                return JobStatus.RUNNING;
            default:
                throw new RuntimeException("not supported");
        }
    }

    private void jobExecute(String command) {
        switch (command) {
            case Command.PUBLISH:
                flinkJob.publish();
                break;
            case Command.SUBMIT:
                flinkJob.submit();
                break;
            case Command.RUN:
                flinkJob.run();
                fireEventListeners(new JobEvent(flinkJob.getActionId(), this));
                break;
            default:
                throw new RuntimeException("not supported");
        }
    }

    static class Restart {

    }
}
