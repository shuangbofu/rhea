package cn.shuangbofu.rhea.web.persist.entity;

import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.Table;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/30 下午12:01
 */
@Data
@Table(name = "rhea_cluster_conf")
@Accessors(chain = true)
public class ClusterConf extends Model<ClusterConf> {
    private Long id;
    private Long gmtCreate;
    private Long gmtModified;
    @Column(name = "`status`")
    private Boolean deleted;
    private String env;

    private String name;
    private Boolean valid;
    private String params;

}
