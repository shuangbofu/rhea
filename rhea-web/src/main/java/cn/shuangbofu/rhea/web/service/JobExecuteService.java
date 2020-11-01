package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.common.DefaultThreadFactory;
import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.job.conf.JobActionProcess;
import cn.shuangbofu.rhea.job.conf.params.ClusterParam;
import cn.shuangbofu.rhea.job.conf.params.ComponentParam;
import cn.shuangbofu.rhea.job.conf.params.Param;
import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import cn.shuangbofu.rhea.job.event.RestartEvent;
import cn.shuangbofu.rhea.job.job.Execution;
import cn.shuangbofu.rhea.job.job.FlinkJob;
import cn.shuangbofu.rhea.job.job.JobManager;
import cn.shuangbofu.rhea.job.job.JobRunner;
import cn.shuangbofu.rhea.job.utils.JSON;
import cn.shuangbofu.rhea.web.persist.dao.ClusterConfDao;
import cn.shuangbofu.rhea.web.persist.dao.ComponentConfDao;
import cn.shuangbofu.rhea.web.persist.dao.Daos;
import cn.shuangbofu.rhea.web.persist.entity.ClusterConf;
import cn.shuangbofu.rhea.web.persist.entity.ComponentConf;
import cn.shuangbofu.rhea.web.vo.param.JobSubmitParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by shuangbofu on 2020/10/30 上午11:35
 */
@Service
public class JobExecuteService implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobExecuteService.class);
    private final ExecutorService executorService = new ThreadPoolExecutor(10, 50,
            60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(),
            new DefaultThreadFactory("execution-pool"));
    private final Map<Long, JobRunner> runnerCache = Maps.newConcurrentMap();

    private final ClusterConfDao clusterConfDao = Daos.clusterConf();
    private final ComponentConfDao componentConfDao = Daos.componentConf();

    @Autowired
    private JobCreator jobCreator;

    @Autowired
    private LogService logService;

    @Autowired
    private AlarmService alarmService;

    public boolean submitExecution(Long actionId, Execution execution) {
        JobRunner runner = getRunner(actionId);
        beforeExecute(runner, execution);
        runner.setParams(getParams(runner.getFlinkJob().getResult().getPublishInfo()));
        if (Execution.STOP.equals(execution)) {
            runner.stop();
            // 停止后关闭日志
            runner.closeLogger();
        } else if (Execution.KILL.equals(execution)) {
            runner.kill();
        } else {
            executorService.execute(() -> {
                try {
                    runner.setup(execution)
                            .execute(execution);
                } catch (Exception e) {
                    if (execution.isRestart()) {
                        if (runner.getRestart().get()) {
                            submitExecution(actionId, execution);
                        } else {
                            runner.logger().error("停止重启!");
                            runner.closeLogger();
                        }
                    } else {
                        runner.logger().error(e.getMessage(), e);
                    }
                } finally {
                    if (!execution.isRestart() ||
                            (execution.isRestart() && runner.isRunning())) {
                        runner.closeLogger();
                    }
                }
            });
        }
        return true;
    }

    public void beforeExecute(JobRunner runner, Execution execution) {
        // 设置参数
        runner.setParams(getParams(runner.getFlinkJob().getResult().getPublishInfo()));
        // 检查状态
        if (Execution.STOP.equals(execution)) {
            if (!runner.isRunning()) {
                throw new RuntimeException("没有在运行中!");
            }
        } else if (Execution.KILL.equals(execution)) {
            if (!runner.isExecuting()) {
                throw new RuntimeException("没有在执行!");
            }
        } else {
            if (Execution.RESTART.equals(execution)) {
                return;
            }
            if (runner.isExecuting()) {
                throw new RuntimeException("正在执行" + runner.getFlinkJob().getResult().getExecution() + "操作,请稍后再操作!");
            }
            JobStatus jobStatus = runner.getFlinkJob().getJobStatus();
            boolean lastExecutionError = execution.equals(runner.getFlinkJob().getResult().getExecution()) && JobStatus.ERROR.equals(jobStatus);
            if (Execution.SUBMIT.equals(execution)) {
                if (!JobStatus.PUBLISHED.equals(jobStatus) &&
                        !lastExecutionError
                ) {
                    throw new RuntimeException("未发布不能提交!");
                }
            } else if (Execution.RUN.equals(execution)) {
                if (!runner.getFlinkJob().isCurrent()) {
                    throw new RuntimeException("已有任务在运行中!");
                }
                if (!JobStatus.SUBMITTED.equals(jobStatus) &&
                        !JobStatus.STOPPED.equals(jobStatus) &&
                        !lastExecutionError &&
                        !(JobStatus.ERROR.equals(jobStatus)) && runner.getFlinkJob().getResult().getExecution().isRestart()
                ) {
                    throw new RuntimeException("当前不能运行，请稍后再试!");
                }
            }
        }
    }

    public JobRunner createRunner(Long actionId) {
        FlinkJob flinkJob = jobCreator.getFlinkJob(actionId);
        List<Param> params = getParams(flinkJob.getResult().getPublishInfo());
        JobRunner jobRunner = new JobRunner(flinkJob, params);
        flinkJob.setRunner(jobRunner);
        jobRunner.addListener(JobManager.INSTANCE);
        jobRunner.addListener(jobCreator);
        jobRunner.addListener(logService);
        jobRunner.addListener(alarmService);
        jobRunner.addListener(this);
        return jobRunner;
    }

    public JobRunner getRunner(Long actionId) {
        return runnerCache.computeIfAbsent(actionId, i -> createRunner(actionId));
    }

    /**
     * 获取最新参数配置（集群、组件）
     *
     * @param info
     * @return
     */
    public List<Param> getParams(JobActionProcess.PublishInfo info) {
        ClusterConf conf = clusterConfDao.findValidOneById(info.getClusterId());
        if (conf == null) {
            throw new RuntimeException("cluster not found");
        }
        ClusterParam clusterParam = JSON.parseObject(conf.getParams(), ClusterParam.class);
        ComponentConf conf2 = componentConfDao.findOneById(info.getComponentId());
        if (conf2 == null) {
            throw new RuntimeException("component not found");
        }
        ComponentParam componentParam = JSON.parseObject(conf2.getParams(), ComponentParam.class);
        return Lists.newArrayList(clusterParam, componentParam);
    }


    @Override
    public void handleEvent(Event event) {
        if (event instanceof RestartEvent) {
            RestartEvent restartEvent = (RestartEvent) event;
            JobRunner runner = getRunner(restartEvent.getActionId());
            runner.getRestart().set(true);
            runner.updateStatusAndResult(JobStatus.ERROR, result -> result.setApplicationId(""));
            submitExecution(restartEvent.getActionId(), Execution.RESTART);
        }
    }

    public void submitCheck(Long actionId, JobSubmitParam param) {
        JobRunner runner = getRunner(actionId);
        if (runner.isExecuting()) {
            throw new RuntimeException("任务正在执行!");
        }
        if (runner.isRunning()) {
            if (!param.getStopCurrent()) {
                throw new RuntimeException("任务正在运行中，请先停止或选择强制重新提交!");
            }
            runner.stop();
        }
        if (runner.getFlinkJob().getJobStatus().equals(JobStatus.SUBMITTED)) {
            if (!param.getStopCurrent()) {
                throw new RuntimeException("任务已有提交，需要提交选择强制提交!");
            }
        }
    }

    public List<JobRunner> getExecutingRunners() {
        return runnerCache.values().stream().filter(JobRunner::isExecuting).collect(Collectors.toList());
    }
}
