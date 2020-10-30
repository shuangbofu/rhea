package cn.shuangbofu.rhea.web.persist.entity;

import cn.shuangbofu.rhea.common.enums.JobType;
import cn.shuangbofu.rhea.job.conf.FlinkConf;
import cn.shuangbofu.rhea.job.conf.FlinkSqlConf;
import cn.shuangbofu.rhea.job.conf.JobText;
import cn.shuangbofu.rhea.job.utils.JSON;
import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.Table;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/18 上午11:26
 */
@Data
@Accessors(chain = true)
@Table(name = "rhea_job_detail")
public class JobDetail extends Model<JobDetail> {
    private Long id;
    private Long gmtCreate;
    private Long gmtModified;
    @Column(name = "`status`")
    private Boolean deleted;
    private String createUser;
    private String modifyUser;
    private String env;

    private Long jobId;
    private int version;
    private String text;
    private String conf;

    public static JobDetail newInstance(JobType type) {
        JobDetail detail = new JobDetail();
        switch (type) {
            case COLLECT:
            case FLINK:
                detail.setText("{}");
                detail.setConf(JSON.toJSONString(new FlinkConf()));
                break;
            case FLINK_SQL:
                detail.setText(JSON.toJSONString(JobText.defaultValue()));
                detail.setConf(JSON.toJSONString(new FlinkSqlConf()));
                break;
            default:
                break;
        }
        return detail;
    }
}
