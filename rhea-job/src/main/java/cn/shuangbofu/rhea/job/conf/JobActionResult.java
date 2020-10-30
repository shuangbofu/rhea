package cn.shuangbofu.rhea.job.conf;

import lombok.Data;

import java.util.List;

/**
 * Created by shuangbofu on 2020/10/18 上午11:34
 */
@Data
public class JobActionResult {
    private PublishInfo publishInfo;
    private String applicationId;
    /**
     * 日志key以 固定日志类型开头。
     * PUBLISH, SUBMIT, RUN(STOP), RESTART(STOP)
     */
    private List<String> logKeys;

    public JobActionResult() {
        publishInfo = new PublishInfo();
    }

    /**
     * 部署集群以及文件位置信息等等
     */
    @Data
    public static class PublishInfo {
        private Long clusterId;
        private Long componentId;
        private String logKey;
        private boolean valid;
        private boolean clean;

        /**
         * 其他配置
         */
    }
}
