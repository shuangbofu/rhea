package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.common.utils.FileUtil;
import cn.shuangbofu.rhea.job.JobLogger;
import cn.shuangbofu.rhea.job.conf.JobActionProcess;
import cn.shuangbofu.rhea.job.conf.params.Param;
import cn.shuangbofu.rhea.job.conf.params.ParamStore;
import cn.shuangbofu.rhea.job.event.ActionUpdateEvent;
import cn.shuangbofu.rhea.job.event.EventHandler;
import cn.shuangbofu.rhea.job.event.JobEvent;
import cn.shuangbofu.rhea.job.event.LogEvent;
import cn.shuangbofu.rhea.job.job.shell.ProcessFailureException;
import cn.shuangbofu.rhea.job.utils.YarnUtil;
import lombok.Getter;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

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
        setExecutor();
    }

    public void setParams(List<Param> params) {
        paramStore.addParams(params);
    }

    private void setExecutor() {
        executor = new RemoteExecutor(paramStore, logger);
    }

    public JobRunner setupLogger(String command) {
        // 创建logger
        String logName = String.format("JOB_%s_%s_%s", flinkJob.getActionId(), System.currentTimeMillis(), command);
        logger = new FileLogger(logName, flinkJob.getJobName(), false);
        logger.info("日志初始化完成");

        setExecutor();
        updateResult(result -> result.start(logger.getKey()));
        fireEventListeners(new LogEvent(logger));
        return this;
    }

    public void restart() {

    }

    public void stop() {
        boolean success = killApp(rsAddress(), flinkJob.getResult().getApplicationId());
        if (!success) {
            throw new RuntimeException("stop job error");
        }
        updateStatusAndResult(JobStatus.STOPPED, result -> result.setApplicationId(""));
        fireEventListeners(new JobEvent(flinkJob.getActionId()));
    }

    public void kill() {
        logger.info("取消当前执行!");
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
        updateStatusAndResult(JobStatus.EXECUTING, result -> result.setExecuteStatus(status));
    }

    private JobStatus getStatus() {
        return flinkJob.getJobStatus();
    }

    private void updateStatus(JobStatus status) {
        flinkJob.updateStatus(status);
        fireEventListeners(new ActionUpdateEvent(flinkJob.getActionId(), status));
    }

    private void updateStatusAndResult(JobStatus status, Consumer<JobActionProcess> consumer) {
        flinkJob.updateStatus(status);
        consumer.accept(flinkJob.getResult());
        fireEventListeners(new ActionUpdateEvent(flinkJob.getActionId(), flinkJob.getResult(), status));
    }

    private void updateResult(Consumer<JobActionProcess> consumer) {
        consumer.accept(flinkJob.getResult());
        fireEventListeners(new ActionUpdateEvent(flinkJob.getActionId(), flinkJob.getResult(), null));
    }

    protected void execute() {

    }

    public void closeLogger() {
        if (logger != null) {
            List<FileUtil.LogResult> logResults = logger.close();
            updateResult(result -> {
                result.end(logger.getKey());
                result.setCurrentLogKey(null);
            });
            fireEventListeners(new LogEvent(logger.getKey(), logResults));
        }
    }

    public RemoteExecutor getExecutor() {
        return executor;
    }

    public JobLogger logger() {
        return logger;
    }

    public ParamStore getParamStore() {
        return paramStore;
    }

    public void execute(String command) {
        synchronized (lock) {
            JobStatus commandStatus = getCommandStatus(command);
            logger.info("任务开始" + command);
            setExecuting(commandStatus);
            try {
                jobExecute(command);
                updateStatus(commandStatus);
            } catch (Exception e) {
                if (e instanceof ProcessFailureException) {
                    ProcessFailureException processFailureException = (ProcessFailureException) e;
                    logger.error(processFailureException.toString());
                } else {
                    logger.error("{}异常", e, command);
                }
                updateStatus(JobStatus.ERROR);
                // 异常之后处理
                checkAfterError(command);
            } finally {
                logger.info("执行结束!");
            }
        }
    }

    private void checkAfterError(String command) {
        if (command.equals(Command.RUN)) {
            try {
                String rsAddress = rsAddress();
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

    private void jobExecute(String command) throws Exception {
        switch (command) {
            case Command.PUBLISH:
                flinkJob.publish();
                break;
            case Command.SUBMIT:
                flinkJob.submit();
                break;
            case Command.RUN:
                flinkJob.run();
                List<String> applicationIds = YarnUtil.getApplicationIds(rsAddress(), flinkJob.getJobName());
                logger.info("yarn上app：{}", applicationIds);
                if (applicationIds.size() > 0) {
                    updateResult(result -> result.setApplicationId(applicationIds.get(0)));
                } else {
                    throw new RuntimeException("启动失败!");
                }
                fireEventListeners(new JobEvent(flinkJob.getActionId(), this));
                break;
            default:
                throw new RuntimeException("not supported");
        }
    }

    public String getApplicationId() {
        return null;
    }

    public String rsAddress() {
        return paramStore.getValue("rsAddress");
    }

    static class Restart {

    }
}
