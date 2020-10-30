package cn.shuangbofu.rhea.job.conf;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

/**
 * Created by shuangbofu on 2020/10/18 下午12:41
 */
@Data
public class FlinkSqlConf extends BaseJobConf {

    private List<Datasource> sources;
    private List<Datasource> sinks;
    private List<Datasource> sides;

    public FlinkSqlConf() {
        super(TYPE_FLINK_SQL);
        sources = Lists.newArrayList();
        sinks = Lists.newArrayList();
        sides = Lists.newArrayList();
    }

    static class Datasource {

    }
}
