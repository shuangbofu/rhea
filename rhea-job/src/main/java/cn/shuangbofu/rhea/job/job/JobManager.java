package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.job.JobLogger;
import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import cn.shuangbofu.rhea.job.event.JobEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shuangbofu on 2020/10/18 下午5:24
 */
public enum JobManager implements EventListener {
    /**
     *
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(JobManager.class);
    @Getter
    private final Map<Long, JobRunner> runningJobs = Maps.newConcurrentMap();
    private final Map<Long, JobLogger> jobLoggers = Maps.newConcurrentMap();
    private final JobCheckThread checkThread = new JobCheckThread();

    public void open(List<JobRunner> runningRunners) {
        runningRunners.forEach(runner -> runningJobs.put(runner.getFlinkJob().getActionId(), runner));
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(checkThread);
        service.shutdown();
    }

    public void shutdown() {
        checkThread.shutdown();
    }

    public JobLogger getLogger(Long actionId) {
        return jobLoggers.computeIfAbsent(actionId, i -> createLogger(actionId));
    }

    private JobLogger createLogger(Long actionId) {
        JobRunner jobRunner = runningJobs.get(actionId);
        return new FileLogger(jobRunner.getFlinkJob().getJobName() + "_" + actionId,
                "/RUNTIME_CHECK/", true);
    }

    @Override
    public void handleEvent(Event event) {
        if (event instanceof JobEvent) {
            JobEvent jobEvent = (JobEvent) event;
            Long actionId = jobEvent.getActionId();
            JobRunner runner = jobEvent.getRunner();
            if (runner == null) {
                runningJobs.remove(actionId);
            } else {
                runningJobs.put(actionId, runner);
            }
        }
    }

    public List<JobLogger> getJobLoggers() {
        return Lists.newArrayList(jobLoggers.values());
    }

    class JobCheckThread extends Thread {

        private boolean shutdown;

        JobCheckThread() {
            setName("running-check");
            setDaemon(true);
            shutdown = false;
        }

        @Override
        public void run() {
            while (!shutdown) {
                synchronized (this) {
                    try {
                        HashMap<Long, JobRunner> checks = Maps.newHashMap(runningJobs);
                        checks.keySet().forEach(actionId -> {
                            JobLogger logger = getLogger(actionId);
                            logger.info("检查任务执行状态");
                        });
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void shutdown() {
            shutdown = true;
        }
    }
}
