package cn.shuangbofu.rhea.web.persist.entity;

import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.Table;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/30 18:42
 */
@Data
@Table(name = "rhea_job_log")
@Accessors(chain = true)
public class JobLog extends Model<JobLog> {

    private Long id;
    private Long gmtCreate;
    private Long gmtModified;
    @Column(name = "`status`")
    private Boolean deleted;
    private String env;

    @Column(name = "`key`")
    private String key;
    private byte[] log;
    private int startByte;
    private int endByte;
}
