package cn.shuangbofu.rhea.job.alarm;

import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/18 上午11:37
 */
@Data
public class AlarmConfig {

    public Integer onceDuration = 2;
    public String retryPreMinutes = "10/2";
}
