package cn.shuangbofu.rhea.web.persist.dao;

import cn.shuangbofu.rhea.web.persist.entity.JobDetail;

import java.util.Optional;

/**
 * Created by shuangbofu on 2020/10/18 上午11:51
 */
public class JobDetailDao extends BaseDao<JobDetail> {
    private final WhereCondition<JobDetail, Long> JOB_ID_WHERE = value -> q -> q.where(JobDetail::getJobId, value);

    public JobDetailDao() {
        super(JobDetail.class);
    }

    public JobDetail findLastedVersion(Long jobId) {
        return findOneBy(q -> JOB_ID_WHERE.where(jobId).apply(q));
    }

    public long newVersion(JobDetail detail) {
        Integer lastedVersion = getLastedVersion(detail.getJobId());
        detail.setVersion(Optional.ofNullable(lastedVersion).orElse(0) + 1);
        return insert(detail);
    }

    public synchronized Integer getLastedVersion(Long jobId) {
        JobDetail version = findOneBy(q -> JOB_ID_WHERE.where(jobId).apply(q.select("version")));
        if (version != null) {
            return version.getVersion();
        }
        return null;
    }

    public int deleteAllByJobId(Long jobId) {
        return deleteBy(q -> q.where(JobDetail::getJobId, jobId));
    }

    public JobDetail getJobIdAndVersion(Long jobId, Integer version) {
        return findOneBy(q -> JOB_ID_WHERE.where(jobId).apply(q.where(JobDetail::getVersion, version)));
    }
}
