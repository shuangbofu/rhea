package cn.shuangbofu.rhea.web.vo;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.common.enums.JobType;
import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/18 下午1:50
 */
@Data
public class ActionQueryParam {
    private String jobName;
    private JobStatus jobStatus;
    private JobType jobType;
    private String createUser;
    private String modifyUser;
    private Long clusterId;
}
