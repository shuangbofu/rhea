package cn.shuangbofu.rhea.web.controller;

import cn.shuangbofu.rhea.common.LogData;
import cn.shuangbofu.rhea.web.service.JobService;
import cn.shuangbofu.rhea.web.vo.*;
import cn.shuangbofu.rhea.web.vo.form.JobForm;
import cn.shuangbofu.rhea.web.vo.param.JobPublishParam;
import cn.shuangbofu.rhea.web.vo.param.JobSubmitParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by shuangbofu on 2020/10/18 上午10:57
 */
@RequestMapping("/api/job")
@RestController
public class JobController {

    @Autowired
    private JobService jobService;

    /**
     * @return 任务列表
     */
    @GetMapping("list")
    public Result<List<JobVO>> listJobs() {
        return Result.success(jobService.listJobs());
    }

    /**
     * 获取任务具体信息
     *
     * @return 具体任务信息
     */
    @GetMapping
    public Result<JobDetailVO> getDetail(Long jobId) {
        return Result.success(jobService.getDetail(jobId));
    }

    /**
     * 创建任务
     *
     * @param form 任务表单
     * @return
     */
    @PostMapping
    public Result<Boolean> createJob(@RequestBody JobForm form) {
        return Result.success(jobService.createJob(form));
    }

    /**
     * 修改任务描述、任务文本及配置
     *
     * @param jobDetailVO
     * @return
     */
    @PutMapping
    public Result<Boolean> modifyJob(@RequestBody JobDetailVO jobDetailVO) {
        return Result.success(jobService.modifyJob(jobDetailVO));
    }

    /**
     * 删除任务
     *
     * @param jobId 任务ID
     * @return
     */
    @DeleteMapping
    public Result<Boolean> removeJob(Long jobId) {
        return Result.success(jobService.removeJob(jobId));
    }

    /**
     * 任务记录分页
     *
     * @param pageQueryParam 过滤条件
     * @return 分页
     */
    @PostMapping("/action/page")
    public Result<PageVO<JobActionVO>> getActionPage(@RequestBody PageQueryParam<ActionQueryParam> pageQueryParam) {
        return Result.success(jobService.getActionPage(pageQueryParam));
    }

    @PostMapping("/publish")
    public Result<Boolean> publishJob(@RequestBody JobPublishParam param) {
        return Result.success(jobService.publishJob(param));
    }

    @PostMapping("/submit")
    public Result<Boolean> submitJob(@RequestBody JobSubmitParam param) {
        return Result.success(jobService.submitJob(param));
    }

    @PostMapping("/run/{actionId}")
    public Result<Boolean> runJob(@PathVariable("actionId") Long actionId) {
        return Result.success(jobService.runJob(actionId));
    }

    @PostMapping("/stop/{actionId}")
    public Result<Boolean> stopJob(@PathVariable("actionId") Long actionId) {
        return Result.success(jobService.stopJob(actionId));
    }

    @PostMapping("/kill/{actionId}")
    public Result<Boolean> killJob(@PathVariable("actionId") Long actionId) {
        return Result.success(jobService.killJob(actionId));
    }

    @GetMapping("/logs")
    public Result<List<LogData>> getHistoryLogs(@RequestParam("actionId") Long actionId,
                                                @RequestParam(name = "limit", required = false, defaultValue = "10") Integer limit) {
        return Result.success(jobService.getHistoryLogs(actionId, limit));
    }

    @GetMapping("/log")
    public Result<LogData> getLog(@RequestParam(name = "actionId") Long actionId,
                                  @RequestParam(name = "offset") Integer offset,
                                  @RequestParam(name = "length", required = false, defaultValue = "3000") Integer length) {
        return Result.success(jobService.getCurrentLog(actionId, offset, length));
    }
}
