package cn.shuangbofu.rhea.job.conf;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by shuangbofu on 2020/10/18 下午12:42
 */
@Data
public abstract class BaseJobConf implements JobConf {
    protected Map<String, Map<String, String>> flinkParams;
    protected Map<String, Map<String, String>> customDefinition;
    protected String type;

    public BaseJobConf(String type) {
        this();
        this.type = type;
    }

    public BaseJobConf() {
        flinkParams = new ConcurrentHashMap<>();
        customDefinition = new ConcurrentHashMap<>();
    }
}
