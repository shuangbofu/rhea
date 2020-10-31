package cn.shuangbofu.rhea.web.persist.dao;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.common.utils.StringUtils;
import cn.shuangbofu.rhea.job.conf.JobActionResult;
import cn.shuangbofu.rhea.job.utils.JSON;
import cn.shuangbofu.rhea.web.persist.entity.JobAction;
import io.github.biezhi.anima.page.Page;

import java.util.List;

/**
 * Created by shuangbofu on 2020/10/18 下午12:16
 */
public class JobActionDao extends BaseDao<JobAction> {
    private final WhereCondition<JobAction, Long> JOB_ID_WHERE = jobId -> q -> q.where(JobAction::getJobId, jobId);

    public JobActionDao() {
        super(JobAction.class);
    }

    public int deleteAllByJobId(Long jobId) {
        return deleteBy(q -> JOB_ID_WHERE.where(jobId).apply(q));
    }

    private Page<JobAction> findAllPageBy(int num, int size, QueryHandler<JobAction> handler) {
        return findPageBy(num, size,
                q -> handler.apply(q.where(JobAction::getCurrent, true)));
    }

    public Page<JobAction> getAllPageByFilter(int num, int size, List<Long> jobIds, JobStatus jobStatus, Long clusterId, String modifyUser) {
        return findAllPageBy(num, size, q -> {
            if (jobStatus != null) {
                q.where(JobAction::getJobStatus, jobStatus);
            }
            if (clusterId != null) {
                q.where(JobAction::getClusterId, clusterId);
            }
            if (StringUtils.isNotEmpty(modifyUser)) {
                q.where(JobAction::getModifyUser, modifyUser);
            }
            q.in(JobAction::getJobId, jobIds);
            return q;
        });
    }

    public JobAction findCurrent(Long jobId) {
        return findOneBy(q -> JOB_ID_WHERE.where(jobId).apply(q.where(JobAction::getJobId, jobId)));
    }

    public int updateResultStatus(Long actionId, JobActionResult result, JobStatus status) {
        return updateById(actionId, q -> {
            if (result != null) {
                q.set(JobAction::getJobActionResult, JSON.toJSONString(result));
            }
            if (status != null) {
                q.set(JobAction::getJobStatus, status);
            }
            q.where(JobAction::getId, actionId);
            return q;
        });
    }

    public void changeCurrent(Long newActionId, Long oldActionId) {
        Daos.atomic(() -> {
            updateById(oldActionId, q -> q.set(JobAction::getCurrent, false));
            updateById(newActionId, q -> q.set(JobAction::getCurrent, true));
        }, "change error");
    }

    public JobActionResult getActionResult(Long actionId) {
        JobAction action = findOneBy(q -> q.select("job_action_result").where("id", actionId));
        if (action != null) {
            return JSON.parseObject(action.getJobActionResult(), JobActionResult.class);
        }
        return null;
    }
}
