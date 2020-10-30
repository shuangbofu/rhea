package cn.shuangbofu.rhea.web.vo;

import cn.shuangbofu.rhea.job.conf.JobConf;
import cn.shuangbofu.rhea.job.conf.JobText;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/18 上午11:28
 */
@Data
@Accessors(chain = true)
public class JobDetailVO extends BaseUserVO<JobDetailVO> {
    private JobVO job;
    private Integer version;
    private JobText text;
    private JobConf conf;
}
