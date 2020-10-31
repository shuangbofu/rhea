package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.common.DefaultThreadFactory;
import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.job.conf.JobActionResult;
import cn.shuangbofu.rhea.job.conf.params.ClusterParam;
import cn.shuangbofu.rhea.job.conf.params.ComponentParam;
import cn.shuangbofu.rhea.job.conf.params.Param;
import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import cn.shuangbofu.rhea.job.job.Command;
import cn.shuangbofu.rhea.job.job.FlinkJob;
import cn.shuangbofu.rhea.job.job.JobManager;
import cn.shuangbofu.rhea.job.job.JobRunner;
import cn.shuangbofu.rhea.job.utils.JSON;
import cn.shuangbofu.rhea.web.persist.dao.ClusterConfDao;
import cn.shuangbofu.rhea.web.persist.dao.ComponentConfDao;
import cn.shuangbofu.rhea.web.persist.dao.Daos;
import cn.shuangbofu.rhea.web.persist.entity.ClusterConf;
import cn.shuangbofu.rhea.web.persist.entity.ComponentConf;
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

/**
 * Created by shuangbofu on 2020/10/30 上午11:35
 */
@Service
public class JobExecuteService implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobExecuteService.class);
    private final ExecutorService executorService = new ThreadPoolExecutor(10, 50,
            60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(),
            new DefaultThreadFactory("execute-manager"));
    private final Map<Long, JobRunner> runnerCache = Maps.newConcurrentMap();

    private final ClusterConfDao clusterConfDao = Daos.clusterConf();
    private final ComponentConfDao componentConfDao = Daos.componentConf();

    @Autowired
    private JobCreator jobCreator;

    @Autowired
    private LogService logService;

    public boolean executeCommand(Long actionId, String command) {
        JobRunner runner = getRunner(actionId);
        runner.setParams(getParams(runner.getFlinkJob().getResult().getPublishInfo()));
        if (Command.STOP.equals(command)) {
            if (!runner.isRunning()) {
                throw new RuntimeException("没有在运行中!");
            }
            runner.stop();
            // 停止后关闭日志
            runner.closeLogger();
        } else if (Command.KILL.equals(command)) {
            if (!runner.isExecuting()) {
                throw new RuntimeException("没有在执行！");
            }
            runner.kill();
        } else {
            executorService.execute(() -> {
                if (Command.SUBMIT.equals(command) && !runner.getFlinkJob().getJobStatus().equals(JobStatus.PUBLISHED)) {
                    throw new RuntimeException("没有发布，不能提交！");
                }
                try {
                    runner.setupLogger(command).execute(command);
                } catch (Exception e) {
                    runner.logger().error(e.getMessage(), e);
                } finally {
                    runner.closeLogger();
                }
            });
        }
        return true;
    }

    public JobRunner createRunner(Long actionId) {
        FlinkJob flinkJob = jobCreator.getFlinkJob(actionId);
        List<Param> params = getParams(flinkJob.getResult().getPublishInfo());
        JobRunner jobRunner = new JobRunner(flinkJob, params);
        flinkJob.setRunner(jobRunner);
        jobRunner.addListener(JobManager.INSTANCE);
        jobRunner.addListener(jobCreator);
        jobRunner.addListener(logService);
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
    public List<Param> getParams(JobActionResult.PublishInfo info) {
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

    }
}
