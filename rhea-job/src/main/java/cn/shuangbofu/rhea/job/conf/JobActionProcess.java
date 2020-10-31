package cn.shuangbofu.rhea.job.conf;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Optional;

/**
 * Created by shuangbofu on 2020/10/18 上午11:34
 */
@Data
public class JobActionProcess {
    private PublishInfo publishInfo;
    private String applicationId;
    private JobStatus executeStatus;
    private String currentLogKey;
    /**
     * 日志key以 固定日志类型开头。
     * PUBLISH, SUBMIT, RUN(STOP), RESTART(STOP)
     */
    private List<String> logKeys = Lists.newArrayList();
    private List<Record> records = Lists.newArrayList();

    public JobActionProcess() {
        publishInfo = new PublishInfo();
    }

    public JobActionProcess(Long clusterId, Long componentId) {
        this();
        getPublishInfo().setClusterId(clusterId);
        getPublishInfo().setComponentId(componentId);
    }

    public void end(String logKey) {
        Optional<Record> any = records.stream().filter(i -> i.getLogKey().equals(logKey)).findAny();
        any.ifPresent(record -> record.setEnd(System.currentTimeMillis()));
    }

    public void start(String logKey) {
        setCurrentLogKey(logKey);
        records.add(new Record(logKey));
    }

    /**
     * 部署集群以及文件位置信息等等
     */
    @Data
    @Accessors(chain = true)
    public static class PublishInfo {
        private Long clusterId;
        private Long componentId;
        /**
         * 其他配置
         */
    }

    @Data
    @Accessors(chain = true)
    public static class Record {
        private String logKey;
        private long start;
        private long end;

        public Record() {
        }

        public Record(String logKey) {
            end = 0;
            start = System.currentTimeMillis();
            this.logKey = logKey;
        }

        public boolean isEnd() {
            return end != 0;
        }
    }
}
