package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.utils.StringUtils;
import cn.shuangbofu.rhea.job.Executor;
import cn.shuangbofu.rhea.job.conf.params.Param;
import cn.shuangbofu.rhea.job.event.EventHandler;
import cn.shuangbofu.rhea.job.utils.YarnUtil;
import lombok.Getter;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by shuangbofu on 2020/10/30 上午11:17
 */
public class JobRunner extends EventHandler implements Runnable {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JobRunner.class);
    @Getter
    private final FlinkJob flinkJob;
    private Restart restart;
    private Executor executor;
    private Logger logger;
    private List<Param> params;

    public JobRunner(FlinkJob flinkJob, List<Param> params) {
        this.flinkJob = flinkJob;
        setParams(params);
    }

    public void setParams(List<Param> params) {
        this.params = params;
        setExecutor();
    }

    private void setExecutor() {

    }

    public JobRunner initLogger(String type) {
        // 创建logger

        return this;
    }

    @Override
    public void run() {

    }

    public void stop() {
        try {
            YarnUtil.kill(getValue("rsAddress"), flinkJob.getResult().getApplicationId());
        } catch (IOException | YarnException e) {
            String msg = "kill flink app error";
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public void publish() {

    }

    public void kill() {

    }

    private boolean isRestart() {
        return restart != null;
    }

    public String getValue(String key) {
        for (Param param : params) {
            String s = param.get(key);
            if (StringUtils.isNotEmpty(s)) {
                return s;
            }
        }
        return null;
    }

    static class Restart {

    }
}
