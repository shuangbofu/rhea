package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.common.utils.FileUtil;
import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import cn.shuangbofu.rhea.job.event.LogEvent;
import cn.shuangbofu.rhea.web.persist.dao.Daos;
import cn.shuangbofu.rhea.web.persist.dao.JobLogDao;
import cn.shuangbofu.rhea.web.persist.entity.JobLog;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by shuangbofu on 2020/10/18 下午7:24
 */
@Service
public class LogService implements EventListener {

    private final JobLogDao jobLogDao = Daos.jobLog();

    @Override
    public void handleEvent(Event event) {
        if (event instanceof LogEvent) {
            LogEvent logEvent = (LogEvent) event;
            List<FileUtil.LogResult> logs = logEvent.getLogs();
            List<JobLog> jobLogs = logs.stream().map(i -> new JobLog().setKey(logEvent.getKey()).setLog(i.getLog()).setStartByte(i.getStartByte()).setEndByte(i.getEndByte()))
                    .collect(Collectors.toList());
            jobLogDao.insertBatch(jobLogs);
        }
    }
}
