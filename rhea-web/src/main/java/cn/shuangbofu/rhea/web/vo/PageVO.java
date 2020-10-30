package cn.shuangbofu.rhea.web.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by shuangbofu on 2019-06-06 10:50
 *
 * @author shuangbofu
 */
@Data
@Builder
public class PageVO<T> implements Serializable {

    private Integer pageNum;

    private Integer pageSize;

    private Long total;

    private List<T> list;

    public PageVO() {
    }

    public PageVO(int pageNum, int pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
    }

    public PageVO(long total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    public PageVO(List<T> list) {
        this.list = list;
    }

    public static <T> PageVO<T> newInstance(int pageNum, int pageSize, long total, List<T> list) {
        return new PageVO<>(pageNum, pageSize, total, list);
    }
}