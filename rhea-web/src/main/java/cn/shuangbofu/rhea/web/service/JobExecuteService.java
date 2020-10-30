package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.common.DefaultThreadFactory;
import cn.shuangbofu.rhea.job.conf.JobActionResult;
import cn.shuangbofu.rhea.job.conf.params.ClusterParam;
import cn.shuangbofu.rhea.job.conf.params.ComponentParam;
import cn.shuangbofu.rhea.job.conf.params.Param;
import cn.shuangbofu.rhea.job.job.FlinkJob;
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
public class JobExecuteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobExecuteService.class);
    private final ExecutorService executorService = new ThreadPoolExecutor(10, 50,
            60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(),
            new DefaultThreadFactory("execute-manager"));
    private final Map<Long, JobRunner> runnerCache = Maps.newConcurrentMap();

    private final ClusterConfDao clusterConfDao = Daos.clusterConf();
    private final ComponentConfDao componentConfDao = Daos.componentConf();

    @Autowired
    private JobManager jobManager;

    public boolean executeCommand(Long actionId, String command) {
        if (Command.STOP.equals(command)) {
            JobRunner jobRunner = jobManager.checkRunning(actionId);
            JobActionResult.PublishInfo info = jobRunner.getFlinkJob().getResult().getPublishInfo();
            jobRunner.setParams(getParams(info.getClusterId(), info.getComponentId()));
            jobRunner.stop();
        } else {
            executorService.execute(() -> {
                if (Command.PUBLISH.equals(command)) {

                }
            });
        }
        return false;
    }

    public JobRunner createRunner(Long actionId) {
        FlinkJob flinkJob = jobManager.getFlinkJob(actionId);

        return null;
    }

    public JobRunner getRunner(Long actionId) {
        return runnerCache.computeIfAbsent(actionId, i -> createRunner(actionId));
    }

    public List<Param> getParams(Long clusterId, Long componentId) {
        ClusterConf conf = clusterConfDao.findOneById(clusterId);
        ClusterParam clusterParam = JSON.parseObject(conf.getParams(), ClusterParam.class);
        ComponentConf conf2 = componentConfDao.findOneById(componentId);
        ComponentParam componentParam = JSON.parseObject(conf2.getParams(), ComponentParam.class);
        return Lists.newArrayList(clusterParam, componentParam);
    }

    public interface Command {
        String SUBMIT = "SUBMIT";
        String STOP = "STOP";
        String RUN = "RUN";
        String PUBLISH = "PUBLISH";
        String KILL = "KILL";
    }
}
