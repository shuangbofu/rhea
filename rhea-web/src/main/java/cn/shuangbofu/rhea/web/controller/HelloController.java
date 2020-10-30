package cn.shuangbofu.rhea.web.controller;

import cn.shuangbofu.rhea.web.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by shuangbofu on 2020/10/30 下午2:47
 */
@RestController
public class HelloController {

    @GetMapping("hello")
    public Result<String> hello() {
        return Result.success("hello rhea!");
    }

}
