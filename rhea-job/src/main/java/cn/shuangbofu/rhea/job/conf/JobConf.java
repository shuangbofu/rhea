package cn.shuangbofu.rhea.job.conf;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by shuangbofu on 2020/10/18 上午11:35
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
public interface JobConf {
    String TYPE_FLINK = "flink";
    String TYPE_FLINK_SQL = "flinkSql";
}
