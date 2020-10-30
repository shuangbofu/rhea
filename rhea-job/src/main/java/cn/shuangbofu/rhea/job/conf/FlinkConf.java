package cn.shuangbofu.rhea.job.conf;

import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/18 下午1:07
 */
@Data
public class FlinkConf extends BaseJobConf {
    private String mainClass;
    private String exeArgs;

    public FlinkConf() {
        super(TYPE_FLINK);
    }
}
