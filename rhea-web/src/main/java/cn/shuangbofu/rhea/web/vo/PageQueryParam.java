package cn.shuangbofu.rhea.web.vo;

import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/18 下午1:49
 */
@Data
public class PageQueryParam<T> {
    private Integer pageSize;
    private Integer pageNum = 1;
    private T filter;
}
