package cn.shuangbofu.rhea.web.controller;

import cn.shuangbofu.rhea.common.LogData;
import cn.shuangbofu.rhea.web.service.LogService;
import cn.shuangbofu.rhea.web.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Created by shuangbofu on 2020/10/31 13:56
 */
@RestController
@RequestMapping("/api/log")
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping
    public Result<LogData> getHistoryLog(@RequestParam("key") String key) {
        return Result.success(logService.getHistoryLog(key));
    }

    @GetMapping("/list")
    public Result<List<LogData>> getHistorys(@RequestParam("keys") String keys) {
        List<String> keyList = Arrays.asList(keys.split(","));
        return Result.success(logService.getHistoryLogs(keyList));
    }
}
