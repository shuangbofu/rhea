package cn.shuangbofu.rhea.web.persist.dao;

import cn.shuangbofu.rhea.web.persist.entity.JobLog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by shuangbofu on 2020/10/30 18:42
 */
public class JobLogDao extends BaseDao<JobLog> {
    protected JobLogDao() {
        super(JobLog.class);
    }

    public List<JobLog> getJobLogByKey(String key) {
        return findListBy(q -> q.where(JobLog::getKey, key));
    }

    public Map<String, List<JobLog>> getJobLogsInKeys(List<String> keys) {
        return findListBy(q -> q.in(JobLog::getKey, keys)).stream()
                .collect(Collectors.groupingBy(JobLog::getKey, LinkedHashMap::new, Collectors.toList()));
    }
}
