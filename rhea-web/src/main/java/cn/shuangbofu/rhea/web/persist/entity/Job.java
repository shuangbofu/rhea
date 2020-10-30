package cn.shuangbofu.rhea.web.persist.entity;

import cn.shuangbofu.rhea.common.enums.JobType;
import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.Table;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/18 上午10:32
 */
@Data
@Table(name = "rhea_job")
@Accessors(chain = true)
public class Job extends Model<Job> {

    private Long id;
    private Long gmtCreate;
    private Long gmtModified;
    @Column(name = "`status`")
    private Boolean deleted;
    private String createUser;
    private String modifyUser;
    private String env;

    private String jobName;
    private JobType jobType;
    private String jobDesc;
    private String alarmConfig;

    public Job() {
        alarmConfig = "{}";
    }
}
