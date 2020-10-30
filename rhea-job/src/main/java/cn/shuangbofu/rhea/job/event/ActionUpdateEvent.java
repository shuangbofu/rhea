package cn.shuangbofu.rhea.job.event;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.job.conf.JobActionResult;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/30 16:41
 */
@Data
@AllArgsConstructor
public class ActionUpdateEvent implements Event {

    private Long actionId;
    private JobActionResult result;
    private JobStatus status;

    public ActionUpdateEvent(Long actionId, JobActionResult result) {
        this(actionId, result, null);
    }

    public ActionUpdateEvent(Long actionId, JobStatus status) {
        this(actionId, null, status);
    }
}
