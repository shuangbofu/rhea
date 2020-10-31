package cn.shuangbofu.rhea.web.persist.entity;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.Table;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/18 上午10:47
 */
@Data
@Table(name = "rhea_job_action")
@Accessors(chain = true)
public class JobAction extends Model<JobAction> {

    private Long id;
    private Long gmtCreate;
    private Long gmtModified;
    @Column(name = "`status`")
    private Boolean deleted;
    private String createUser;
    private String modifyUser;
    private String env;

    private Long jobId;
    private Integer jobVersion;
    private String jobActionProcess;
    private String publishDesc;
    private JobStatus jobStatus;

    private Long clusterId;
    private Long componentId;

    private Boolean current;

    public JobAction() {
    }
}
