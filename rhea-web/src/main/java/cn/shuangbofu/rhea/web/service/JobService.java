package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.job.conf.JobActionConf;
import cn.shuangbofu.rhea.job.conf.JobActionResult;
import cn.shuangbofu.rhea.job.conf.JobConf;
import cn.shuangbofu.rhea.job.conf.JobText;
import cn.shuangbofu.rhea.job.utils.JSON;
import cn.shuangbofu.rhea.web.CurrentLoginUser;
import cn.shuangbofu.rhea.web.persist.dao.Daos;
import cn.shuangbofu.rhea.web.persist.dao.JobActionDao;
import cn.shuangbofu.rhea.web.persist.dao.JobDao;
import cn.shuangbofu.rhea.web.persist.dao.JobDetailDao;
import cn.shuangbofu.rhea.web.persist.entity.Job;
import cn.shuangbofu.rhea.web.persist.entity.JobAction;
import cn.shuangbofu.rhea.web.persist.entity.JobDetail;
import cn.shuangbofu.rhea.web.vo.*;
import cn.shuangbofu.rhea.web.vo.form.JobForm;
import cn.shuangbofu.rhea.web.vo.param.JobPublishParam;
import cn.shuangbofu.rhea.web.vo.param.JobSubmitParam;
import io.github.biezhi.anima.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by shuangbofu on 2020/10/18 上午11:02
 */
@Service
public class JobService {
    private final JobDao jobDao = Daos.job();
    private final JobDetailDao jobDetailDao = Daos.jobDetail();
    private final JobActionDao jobActionDao = Daos.jobAction();

    @Autowired
    private JobManager jobManager;

    @Autowired
    private JobExecuteService jobExecuteService;

    public List<JobVO> listJobs() {
        return jobDao.findAll().stream().map(this::job2VO).collect(Collectors.toList());
    }

    private JobVO job2VO(Job job) {
        return new JobVO()
                .setId(job.getId())
                .setJobName(job.getJobName())
                .setJobType(job.getJobType())
                .setJobDesc(job.getJobDesc())
                .setGmtCreate(job.getGmtCreate())
                .setGmtModified(job.getGmtModified())
                .setCreateUser(job.getCreateUser())
                .setModifyUser(job.getModifyUser());
    }

    public JobDetailVO getDetail(Long jobId) {
        JobDetail detail = jobDetailDao.findLastedVersion(jobId);
        Job job = jobDao.findOneById(jobId);
        return new JobDetailVO()
                .setJob(job2VO(job))
                .setVersion(detail.getVersion())
                .setConf(JSON.parseObject(detail.getConf(), JobConf.class))
                .setText(JSON.parseObject(detail.getText(), JobText.class))
                .setGmtCreate(detail.getGmtCreate())
                .setGmtModified(detail.getGmtModified())
                .setCreateUser(detail.getCreateUser())
                .setModifyUser(detail.getModifyUser());
    }

    public boolean createJob(JobForm form) {
        String loginUser = CurrentLoginUser.getUser();
        Daos.atomic(() -> {
            Job job = new Job().setJobName(form.getJobName())
                    .setJobType(form.getJobType())
                    .setJobDesc(form.getJobDesc())
                    .setCreateUser(loginUser)
                    .setModifyUser(loginUser);
            Long jobId = jobDao.insert(job);
            JobDetail detail = JobDetail.newInstance(job.getJobType())
                    .setJobId(jobId)
                    .setCreateUser(loginUser)
                    .setModifyUser(loginUser);
            jobDetailDao.newVersion(detail);
        }, "create job error");
        return true;
    }

    public boolean removeJob(Long jobId) {
        // TODO 检查任务状态
        Daos.atomic(() -> {
            jobDao.deleteById(jobId);
            jobDetailDao.deleteAllByJobId(jobId);
            jobActionDao.deleteAllByJobId(jobId);
        }, "remove job error");
        return true;
    }

    private JobActionVO action2VO(Job job, JobAction action) {
        return new JobActionVO()
                .setJob(job2VO(job))
                .setVersion(action.getVersion())
                .setJobStatus(action.getStatus())
                .setPublishDesc(action.getPublishDesc())
                .setConf(JSON.parseObject(action.getJobActionConf(), JobActionConf.class))
                .setResult(JSON.parseObject(action.getJobActionResult(), JobActionResult.class));
    }

    public PageVO<JobActionVO> getActionPage(PageQueryParam<ActionQueryParam> pageQueryParam) {
        int pageNum = pageQueryParam.getPageNum();
        Integer pageSize = pageQueryParam.getPageSize();
        ActionQueryParam filter = pageQueryParam.getFilter();
        List<Long> jobIds = jobDao.getIdsByFilter(filter.getCreateUser(), filter.getJobType(), filter.getJobName());
        Page<JobAction> page = jobActionDao.getAllPageByFilter(pageNum, pageSize, jobIds, filter.getJobStatus(), filter.getClusterId(), filter.getModifyUser());
        List<JobActionVO> actions = page.getRows().stream().map(i -> {
            Job job = jobDao.findOneById(i.getJobId());
            return action2VO(job, i);
        }).collect(Collectors.toList());

        return PageVO.newInstance(pageNum, pageSize, page.getTotalRows(), actions);
    }

    public boolean modifyJob(JobDetailVO jobDetailVO) {
        String loginUser = CurrentLoginUser.getUser();
        JobVO jobVO = jobDetailVO.getJob();
        jobDao.updateDesc(jobVO.getId(), jobVO.getJobDesc(), loginUser);
        JobDetail newVersion = new JobDetail()
                .setJobId(jobVO.getId())
                .setConf(JSON.toJSONString(jobDetailVO.getConf()))
                .setText(JSON.toJSONString(jobDetailVO.getText()))
                .setCreateUser(loginUser)
                .setModifyUser(loginUser);
        jobDetailDao.newVersion(newVersion);
        return true;
    }

    public boolean publishJob(JobPublishParam param) {
        JobAction action = new JobAction()
                .setJobId(param.getJobId())
                .setStatus(JobStatus.PENDING)
                .setVersion(param.getVersion())
                .setCurrent(false)
                .setComponentId(param.getComponentId())
                .setClusterId(param.getClusterId())
                .setPublishDesc(param.getPublishDesc())
//                .setJobActionConf(JSON.toJSONString(new JobActionConf()))
                .setJobActionResult(JSON.toJSONString(new JobActionResult()))
                .setCreateUser(CurrentLoginUser.getUser())
                .setModifyUser(CurrentLoginUser.getUser());
        Long actionId = jobActionDao.insert(action);
        // TODO 部署到集群上，生成记录，记录部署日志
        return jobExecuteService.executeCommand(actionId, JobExecuteService.Command.PUBLISH);
    }

    public boolean submitJob(JobSubmitParam param) {
        JobAction action = jobActionDao.findOneById(param.getActionId());
        Long jobId = action.getJobId();
        JobAction current = jobActionDao.findCurrent(jobId);

        // TODO 检查集群组件配置是否异常，异常提示重新设置执行环境配置。（修改过程也需要记录到日志）

        // TODO 检查当前是否可以执行，如运行中需要强制停止

        // TODO 提交，复制发布目录配置文件到执行目录
        return jobExecuteService.executeCommand(action.getId(), JobExecuteService.Command.SUBMIT);
    }

    public boolean runJob(Long actionId) {
        // 执行启动任务
        return jobExecuteService.executeCommand(actionId, JobExecuteService.Command.RUN);
    }

    public boolean stopJob(Long actionId) {
        // 停止运行中的任务
        return jobExecuteService.executeCommand(actionId, JobExecuteService.Command.STOP);
    }

    public boolean killJob(Long actionId) {
        // 停止启动中的任务
        return jobExecuteService.executeCommand(actionId, JobExecuteService.Command.KILL);
    }
}
