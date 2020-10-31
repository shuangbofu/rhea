package cn.shuangbofu.rhea.web.controller;

import cn.shuangbofu.rhea.web.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Optional;

import static org.springframework.http.HttpStatus.OK;

/**
 * Created by shuangbofu on 2020/8/5 15:20
 */
@Slf4j
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    @ResponseStatus(OK)
    @ExceptionHandler(RuntimeException.class)
    public Result<Object> runtime(RuntimeException e) {
        String message = Optional.ofNullable(e.getMessage()).orElse("null pointer");
        log.warn(message, e);
        return Result.fail(-1, message);
    }
}
