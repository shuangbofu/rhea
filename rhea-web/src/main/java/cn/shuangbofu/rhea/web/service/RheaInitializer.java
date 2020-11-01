package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.common.utils.FileUtil;
import cn.shuangbofu.rhea.job.conf.JobActionProcess;
import cn.shuangbofu.rhea.job.job.Execution;
import cn.shuangbofu.rhea.job.job.FileLogger;
import cn.shuangbofu.rhea.job.job.JobManager;
import cn.shuangbofu.rhea.job.job.JobRunner;
import cn.shuangbofu.rhea.job.utils.JSON;
import cn.shuangbofu.rhea.web.persist.dao.Daos;
import cn.shuangbofu.rhea.web.persist.dao.JobActionDao;
import cn.shuangbofu.rhea.web.persist.dao.JobDao;
import cn.shuangbofu.rhea.web.persist.dao.JobLogDao;
import cn.shuangbofu.rhea.web.persist.entity.Job;
import cn.shuangbofu.rhea.web.persist.entity.JobAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by shuangbofu on 2020/11/1 上午11:44
 */
@Configuration
public class RheaInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RheaInitializer.class);

    private final JobActionDao jobActionDao = Daos.jobAction();
    private final JobDao jobDao = Daos.job();
    private final JobLogDao jobLogDao = Daos.jobLog();

    @Autowired
    private JobExecuteService jobExecuteService;
    @Autowired
    private LogService logService;

    @PostConstruct
    public void init() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 恢复丢失的日志，持久化到数据库
            restoreLog();
            // 初始化正在运行的任务，开启检查
            runningJobsCheck();
            // 修复执行中的任务
            fixExecutingJobs();
            // 添加异常退出钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                /*
                    程序异常时
                    正在执行中的任务和任务状态检查
                    打印错误日志
                 */
                String errorMsg = "程序异常退出!";
                jobExecuteService.getExecutingRunners().forEach(runner -> runner.logger().error(errorMsg));
                JobManager.INSTANCE.getJobLoggers().forEach(logger -> logger.error(errorMsg));
                JobManager.INSTANCE.shutdown();
            }));
            // 数据订正
            fixData();
        });
    }

    private void fixData() {

    }

    private void restoreLog() {
        List<JobAction> actions = jobActionDao.findAll();
        actions.forEach(action -> {
            Job job = jobDao.findOneById(action.getJobId());
            JobActionProcess process = JSON.parseObject(action.getJobActionProcess(), JobActionProcess.class);
            String key = process.getCurrentLogKey();
            if (key != null) {
                long count = jobLogDao.findCountByKey(key);
                if (count == 0) {
                    process.getRecords().stream().filter(i -> i.getLogKey().equals(key))
                            .forEach(i -> i.setEnded(true));
                    process.setCurrentLogKey(null);
                    // 如果是正在执行因为程序异常退出而导致的异常则置为执行中
                    JobStatus newStatus = action.getCurrent() ? JobStatus.EXECUTING : null;
                    jobActionDao.updateResultStatus(action.getId(), process, newStatus);
                    String filePath = "./logs/" + job.getJobName() + "/" + key + ".log";
                    File file = new File(filePath);
                    if (file.exists()) {
                        LOGGER.info("恢复日志 {}", filePath);
                        List<FileUtil.LogResult> logs = FileLogger.getLogsFromFile(file);
                        logService.saveLogs(key, logs);
                    }
                }
            }
        });
    }

    private void fixExecutingJobs() {
        List<JobAction> actions = jobActionDao.getActionsByStatus(JobStatus.EXECUTING);
        actions.forEach(action -> {
            // 置为失败
            LOGGER.info("{}设置为失败", action.getId());
            jobActionDao.updateResultStatus(action.getId(), null, JobStatus.ERROR);
            JobActionProcess actionProcess = JSON.parseObject(action.getJobActionProcess(), JobActionProcess.class);
            Execution execution = actionProcess.getExecution();
            if (actionProcess.getExecution().equals(Execution.PUBLISH) || action.getCurrent()) {
                // 重新执行
                LOGGER.info("{}重新{}", action.getId(), execution);
                jobExecuteService.submitExecution(action.getId(), execution);
            }
        });
    }

    private void runningJobsCheck() {
        List<Long> actionIds = jobActionDao.getActionIdsByStatus(JobStatus.RUNNING);
        List<JobRunner> runners = actionIds.stream().map(i -> jobExecuteService.getRunner(i)).collect(Collectors.toList());
        JobManager.INSTANCE.open(runners);
    }
}
