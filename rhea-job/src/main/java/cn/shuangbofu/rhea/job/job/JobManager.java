package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.job.JobLogger;
import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import cn.shuangbofu.rhea.job.event.JobEvent;
import cn.shuangbofu.rhea.job.event.RestartEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
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
            if (jobEvent.running()) {
                runningJobs.put(actionId, jobEvent.getRunner());
            } else {
                runningJobs.remove(actionId);
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
                        Map<Long, JobRunner> checks = Maps.newConcurrentMap();
                        checks.putAll(runningJobs);
                        checks.keySet().forEach(actionId -> {
                            JobLogger logger = getLogger(actionId);
                            JobRunner jobRunner = checks.get(actionId);
                            // TODO 检查任务状态
                            boolean mock = mock(actionId);
                            logger.info("检查任务执行状态:{}", mock ? "RUNNING" : "ERROR");
                            if (!mock) {
                                runningJobs.remove(actionId);
                                jobRunner.fireEventListeners(new RestartEvent(actionId));
                            }
                        });
                        Thread.sleep(4000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void shutdown() {
            shutdown = true;
        }

        private boolean mock(Long actionId) {
            return new SecureRandom().nextInt(10) > 9;
        }
    }
}
