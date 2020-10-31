package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import cn.shuangbofu.rhea.job.event.JobEvent;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    JobManager() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(new JobCheckThread());
        service.shutdown();
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
