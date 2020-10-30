package cn.shuangbofu.rhea.web.persist.entity;

import io.github.biezhi.anima.annotation.Column;
import io.github.biezhi.anima.annotation.Table;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/30 下午1:13
 */
@Data
@Table(name = "rhea_component_conf")
@Accessors(chain = true)
public class ComponentConf extends Model<ComponentConf> {
    private Long id;
    private Long gmtCreate;
    private Long gmtModified;
    @Column(name = "`status`")
    private Boolean deleted;
    private String env;

    private String name;
    private String params;
    private Long clusterId;
    private String type;
}
