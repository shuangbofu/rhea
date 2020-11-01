package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.common.utils.DateUtils;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Created by shuangbofu on 2020/10/30 上午11:17
 */
public class JobRunner extends EventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);
    @Getter
    private final Long actionId;
    @Getter
    private final FlinkJob flinkJob;
    private final Object lock = new Object();
    private final ParamStore paramStore;
    @Getter
    private final AtomicBoolean restart = new AtomicBoolean(true);
    private RemoteExecutor executor;
    private JobLogger logger;

    public JobRunner(FlinkJob flinkJob, List<Param> params) {
        this.flinkJob = flinkJob;
        actionId = flinkJob.getActionId();
        paramStore = new ParamStore();
        setParams(params);
        initExecutor();
    }

    public String getName() {
        return flinkJob.getJobName() + "_" + actionId;
    }

    public void setParams(List<Param> params) {
        paramStore.addParams(params);
    }

    private void initExecutor() {
        executor = new RemoteExecutor(paramStore, logger);
    }

    public JobRunner setup(Execution execution) {
        if (!execution.isRestart() || logger == null || (logger.closed() && execution.isRestart())) {
            // 创建logger
            String logName = String.format("JOB_%s_%s_%s", flinkJob.getActionId(), DateUtils.now(), execution);
            logger = new FileLogger(logName, flinkJob.getJobName(), false);
            logger.info("日志初始化完成");
            updateResult(result -> result.start(logger.getKey()));
            fireEventListeners(new LogEvent(logger));
        }

        // 初始化执行器
        initExecutor();
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
        if (restart.get()) {
            restart.set(false);
        }
        logger.info("取消当前执行!");
        executor.cancel();
    }

    public boolean isRunning() {
        return JobStatus.RUNNING.equals(getStatus());
    }

    public boolean isExecuting() {
        return JobStatus.EXECUTING.equals(getStatus());
    }

    private JobStatus getStatus() {
        return flinkJob.getJobStatus();
    }

    public void updateStatus(JobStatus status) {
        flinkJob.updateStatus(status);
        fireEventListeners(new ActionUpdateEvent(flinkJob.getActionId(), status));
    }

    public void updateStatusAndResult(JobStatus status, Consumer<JobActionProcess> consumer) {
        flinkJob.updateStatus(status);
        consumer.accept(flinkJob.getResult());
        fireEventListeners(new ActionUpdateEvent(flinkJob.getActionId(), flinkJob.getResult(), status));
    }

    public void updateResult(Consumer<JobActionProcess> consumer) {
        consumer.accept(flinkJob.getResult());
        fireEventListeners(new ActionUpdateEvent(flinkJob.getActionId(), flinkJob.getResult(), null));
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

    public void execute(Execution execution) {
        synchronized (lock) {
            logger.info("任务开始" + execution);
            updateStatusAndResult(JobStatus.EXECUTING, result ->
                    result.setExecution(execution)
            );
            try {
                execute0(execution);
                updateStatus(execution.getFinalStatus());
            } catch (Exception e) {
                if (e instanceof ProcessFailureException) {
                    ProcessFailureException processFailureException = (ProcessFailureException) e;
                    logger.error(processFailureException.toString());
                } else {
                    logger.error("{}异常", e, execution);
                }
                if (!execution.isRestart() ||
                        (execution.isRestart() && !restart.get())
                ) {
                    updateStatus(JobStatus.ERROR);
                }
                // 异常之后处理
                checkAfterError(execution);
                throw new RuntimeException(e);
            } finally {
                logger.info("执行结束!");
            }
        }
    }

    private void checkAfterError(Execution execution) {
        if (execution.equals(Execution.RUN)) {
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

    private void execute0(Execution execution) throws Exception {
        switch (execution) {
            case PUBLISH:
                flinkJob.publish();
                break;
            case SUBMIT:
                flinkJob.submit();
                break;
            case RESTART:
            case RUN:
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JobRunner jobRunner = (JobRunner) o;
        return Objects.equals(actionId, jobRunner.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId);
    }
}
