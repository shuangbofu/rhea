package cn.shuangbofu.rhea.web.vo.param;

import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/18 下午3:58
 */
@Data
public class JobPublishParam {
    private Long jobId;
    private Integer version;
    private Long clusterId;
    private Long componentId;
    private String publishDesc;
}
