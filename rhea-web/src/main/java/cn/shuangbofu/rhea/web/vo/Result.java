package cn.shuangbofu.rhea.web.vo;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * Created by shuangbofu on 2019-06-05 15:34
 */
@Getter
@Setter
@ToString
public class Result<T> implements Serializable {
    private static final long serialVersionUID = -7580239815152036051L;
    private String message;
    private int code;
    private T data;
    private boolean success;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = HttpStatus.OK.value();
        result.data = data;
        result.success = true;
        result.message = "";
        return result;
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = msg;
        result.success = false;
        return result;
    }
}
