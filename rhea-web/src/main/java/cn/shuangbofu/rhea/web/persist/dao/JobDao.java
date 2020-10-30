package cn.shuangbofu.rhea.web.persist.dao;

import cn.shuangbofu.rhea.common.enums.JobType;
import cn.shuangbofu.rhea.common.utils.StringUtils;
import cn.shuangbofu.rhea.web.persist.entity.Job;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by shuangbofu on 2020/10/18 上午10:29
 */
public class JobDao extends BaseDao<Job> {
    public JobDao() {
        super(Job.class);
    }

    public int updateDesc(Long id, String jobDesc, String user) {
        return updateById(id, q -> q.set(Job::getJobDesc, jobDesc).set(Job::getCreateUser, user));
    }

    public List<Long> getIdsByFilter(String createUser, JobType jobType, String jobName) {
        return findListBy(q -> {
            q.select("id");
            if (StringUtils.isNotEmpty(createUser)) {
                q.where(Job::getCreateUser, createUser);
            }
            if (jobType != null) {
                q.where(Job::getJobType, jobType);
            }
            if (StringUtils.isNotEmpty(jobName)) {
                q.like(Job::getJobName, "%s" + jobName + "%s");
            }
            return q;
        }).stream().map(Job::getId).collect(Collectors.toList());
    }
}
