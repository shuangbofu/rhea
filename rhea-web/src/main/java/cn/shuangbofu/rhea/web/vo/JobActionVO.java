package cn.shuangbofu.rhea.web.vo;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.job.conf.JobActionResult;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/18 下午1:15
 */
@Data
@Accessors(chain = true)
public class JobActionVO extends BaseUserVO<JobActionVO> {
    private JobVO job;
    private Integer version;
    private String publishDesc;
    private JobStatus jobStatus;
    private JobActionResult result;
}
