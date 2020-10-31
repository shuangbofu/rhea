package cn.shuangbofu.rhea.web.controller;

import cn.shuangbofu.rhea.web.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by shuangbofu on 2020/10/31 13:56
 */
@RestController("/api/log")
public class LogController {

    @Autowired
    private LogService logService;
}
