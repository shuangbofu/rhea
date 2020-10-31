package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.common.LogData;
import cn.shuangbofu.rhea.common.utils.FileUtil;
import cn.shuangbofu.rhea.job.JobLogger;
import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import cn.shuangbofu.rhea.job.event.LogEvent;
import cn.shuangbofu.rhea.web.persist.dao.Daos;
import cn.shuangbofu.rhea.web.persist.dao.JobLogDao;
import cn.shuangbofu.rhea.web.persist.entity.JobLog;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by shuangbofu on 2020/10/18 下午7:24
 */
@Service
public class LogService implements EventListener {

    private final JobLogDao jobLogDao = Daos.jobLog();
    private final Map<String, JobLogger> loggerCache = new ConcurrentHashMap<>();

    private byte[] combineArrays(List<byte[]> a) {
        int massLength = 0;
        for (byte[] b : a) {
            massLength += b.length;
        }
        byte[] c = new byte[massLength];
        byte[] d;
        int index = 0;
        for (byte[] anA : a) {
            d = anA;
            System.arraycopy(d, 0, c, index, d.length);
            index += d.length;
        }
        return c;
    }

    @Override
    public void handleEvent(Event event) {
        if (event instanceof LogEvent) {
            LogEvent logEvent = (LogEvent) event;
            String key = logEvent.getKey();
            List<FileUtil.LogResult> logs = logEvent.getLogs();
            if (logs != null && logs.size() > 0) {
                List<JobLog> jobLogs = logs.stream().map(i -> new JobLog().setKey(key).setLog(i.getLog()).setStartByte(i.getStartByte()).setEndByte(i.getEndByte()))
                        .collect(Collectors.toList());
                jobLogDao.insertBatch(jobLogs);
                loggerCache.remove(key);
            } else {
                loggerCache.put(key, logEvent.getLogger());
            }
        }
    }

    public LogData getHistoryLog(String key) {
        return modelListData2Log(jobLogDao.getJobLogByKey(key));
    }

    public LogData getJobLog(String key, Integer offset, Integer length) {
        JobLogger jobLogger = loggerCache.get(key);
        return jobLogger.getLog(offset, length);
    }

    public Map<String, LogData> getHistoryLogs(List<String> keys) {
        Map<String, List<JobLog>> jobLogsInKeys = jobLogDao.getJobLogsInKeys(keys);
        return jobLogsInKeys.keySet().stream()
                .collect(Collectors.toMap(i -> i, i -> modelListData2Log(jobLogsInKeys.get(i)), (i, j) -> j, LinkedHashMap::new));
    }

    private LogData modelListData2Log(List<JobLog> jobLogs) {
        if (jobLogs == null || jobLogs.size() == 0) {
            return new LogData(0, 0, "");
        }
        jobLogs.sort(Comparator.comparingInt(JobLog::getStartByte));
        List<byte[]> byteList = jobLogs.stream().map(JobLog::getLog).collect(Collectors.toList());
        byte[] bytes = combineArrays(byteList);
        String s = new String(bytes, StandardCharsets.UTF_8);
        return new LogData(0, s.length(), s, jobLogs.stream().map(JobLog::getGmtCreate)
                .max(Comparator.comparingInt(Long::intValue)).orElse(0L));
    }
}
