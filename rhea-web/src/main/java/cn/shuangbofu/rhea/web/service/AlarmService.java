package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.common.DefaultThreadFactory;
import cn.shuangbofu.rhea.job.alarm.AlarmConfig;
import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import cn.shuangbofu.rhea.job.event.JobEvent;
import cn.shuangbofu.rhea.job.event.RestartEvent;
import cn.shuangbofu.rhea.job.job.JobRunner;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by shuangbofu on 2020/11/1 下午3:26
 */
@Component
public class AlarmService implements EventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmService.class);

    private final Map<JobRunner, Recorder> recordCache = new ConcurrentHashMap<>();
    @Autowired
    private JobExecuteService jobExecuteService;

    {
        Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory("restore-check"))
                .scheduleAtFixedRate(() -> recordCache.keySet().forEach(runner -> {
                    Recorder recorder = recordCache.get(runner);
                    AlarmConfig config = runner.getFlinkJob().getAlarmConfig();
                    if (recorder.duration() > 1000 * 60 * config.getOnceDuration()) {
                        // 持续时间大于XXX发送通知
                        LOGGER.info("{} {}分钟内没有运行成功!", runner.getName(), config.getOnceDuration());
                    }
                    String[] split = config.getRetryPreMinutes().split("/");
                    int preMinutes = Integer.parseInt(split[1]);
                    int retryCount = Integer.parseInt(split[0]);
                    long count = recorder.count(1000L * 60 * preMinutes);
                    if (count > retryCount) {
                        // 固定时间内重启次数大于XXX发送通知
                        LOGGER.error("{} {}分钟内重启次数{}大于{}!", runner.getName(), preMinutes, count, retryCount);
                    }
                }), 10, 5, TimeUnit.SECONDS);
    }

    @Override
    public void handleEvent(Event event) {
        if (event instanceof RestartEvent) {
            Long actionId = ((RestartEvent) event).getActionId();
            JobRunner runner = jobExecuteService.getRunner(actionId);
            // 重启发送通知
            LOGGER.error("{}重启!", runner.getName());
            getRecorder(runner).record();
        } else if (event instanceof JobEvent) {
            JobEvent jobEvent = (JobEvent) event;
            getRecorder(jobEvent.getRunner()).end();
        }
    }

    public Recorder getRecorder(JobRunner runner) {
        return recordCache.computeIfAbsent(runner, i -> new Recorder());
    }

    static class Recorder {
        private final List<Record> records = new ArrayList<>();

        public void record() {
            records.add(new Record());
        }

        public long duration() {
            Record recent = recent();
            if (recent != null && !recent.isEnd()) {
                return System.currentTimeMillis() - recent.getTimestamp();
            }
            return -1L;
        }

        public long count(long millis) {
            return records.stream()
                    .filter(record -> System.currentTimeMillis() - record.getTimestamp() <= millis)
                    .count();
        }

        public void end() {
            Record recent = recent();
            if (recent != null) {
                recent.end();
            }
        }

        private Record recent() {
            if (records.size() > 0) {
                return records.get(0);
            }
            return null;
        }


        static class Record {
            @Getter
            private final Long timestamp;
            private Long end = -1L;

            public Record() {
                timestamp = System.currentTimeMillis();
            }

            public void end() {
                end = System.currentTimeMillis();
            }

            public boolean isEnd() {
                return end != -1;
            }
        }
    }
}
