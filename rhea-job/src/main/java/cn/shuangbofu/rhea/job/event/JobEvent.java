package cn.shuangbofu.rhea.job.event;

import cn.shuangbofu.rhea.job.job.JobRunner;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by shuangbofu on 2020/10/18 下午7:16
 */
@Data
@Accessors(chain = true)
public class JobEvent implements Event {
    private Long actionId;
    private JobRunner runner;

    public JobEvent(Long actionId) {
        this.actionId = actionId;
    }

    public JobEvent(Long actionId, JobRunner runner) {
        this(actionId);
        this.runner = runner;
    }

    public boolean running() {
        return runner != null;
    }
}
