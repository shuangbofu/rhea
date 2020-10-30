package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import cn.shuangbofu.rhea.job.job.FlinkJob;
import cn.shuangbofu.rhea.job.job.JobRunner;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by shuangbofu on 2020/10/18 下午5:24
 */
@Component
public class JobManager implements EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobManager.class);
    private final Map<Long, FlinkJob> flinkJobCache = Maps.newConcurrentMap();
    @Getter
    private final Map<Long, JobRunner> runningJobs = Maps.newConcurrentMap();
    @Autowired
    private JobCreator jobCreator;

    public JobManager() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(new JobCheckThread());
        service.shutdown();
    }

    public FlinkJob getFlinkJob(Long actionId) {
        return flinkJobCache.computeIfAbsent(actionId, i -> jobCreator.createJob(actionId));
    }

    @Override
    public void handleEvent(Event event) {

    }

    static class JobCheckThread extends Thread {
        JobCheckThread() {
            setName("running-check");
            setDaemon(true);
        }

        @Override
        public void run() {

        }
    }
}
