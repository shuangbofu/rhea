package cn.shuangbofu.rhea.web.vo.form;

import cn.shuangbofu.rhea.common.enums.JobType;
import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/18 下午12:23
 */
@Data
public class JobForm {
    private String jobName;
    private JobType jobType;
    private String jobDesc;
}
