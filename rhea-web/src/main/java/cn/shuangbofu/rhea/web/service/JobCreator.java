package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.common.enums.JobType;
import cn.shuangbofu.rhea.job.conf.JobConf;
import cn.shuangbofu.rhea.job.conf.JobText;
import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import cn.shuangbofu.rhea.job.job.FlinkJarJob;
import cn.shuangbofu.rhea.job.job.FlinkJob;
import cn.shuangbofu.rhea.job.job.FlinkSqlJob;
import cn.shuangbofu.rhea.job.utils.JSON;
import cn.shuangbofu.rhea.web.persist.dao.Daos;
import cn.shuangbofu.rhea.web.persist.dao.JobActionDao;
import cn.shuangbofu.rhea.web.persist.dao.JobDao;
import cn.shuangbofu.rhea.web.persist.dao.JobDetailDao;
import cn.shuangbofu.rhea.web.persist.entity.Job;
import cn.shuangbofu.rhea.web.persist.entity.JobAction;
import cn.shuangbofu.rhea.web.persist.entity.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by shuangbofu on 2020/10/18 下午7:14
 */
@Component
public class JobCreator implements EventListener {

    private final JobDao jobDao = Daos.job();
    private final JobDetailDao jobDetailDao = Daos.jobDetail();
    private final JobActionDao jobActionDao = Daos.jobAction();

    @Autowired
    private LogService logService;

    @Autowired
    private JobManager jobManager;

    public FlinkJob createJob(Long actionId) {
        JobAction action = jobActionDao.findOneById(actionId);
        Long jobId = action.getJobId();
        Integer version = action.getVersion();
        JobDetail jobDetail = jobDetailDao.getJobIdAndVersion(jobId, version);
        Job job = jobDao.findOneById(jobId);
        JobType jobType = job.getJobType();
        FlinkJob flinkJob;
        if (jobType.equals(JobType.FLINK_SQL)) {
            flinkJob = new FlinkSqlJob(jobId, job.getJobName(), actionId, action.getStatus(),
                    JSON.parseObject(jobDetail.getText(), JobText.class),
                    JSON.parseObject(jobDetail.getConf(), JobConf.class));
        } else {
            flinkJob = new FlinkJarJob(jobId, job.getJobName(), actionId, action.getStatus(),
                    JSON.parseObject(jobDetail.getText(), JobText.class),
                    JSON.parseObject(jobDetail.getConf(), JobConf.class));
        }
        return flinkJob;
    }

    @Override
    public void handleEvent(Event event) {

    }
}
