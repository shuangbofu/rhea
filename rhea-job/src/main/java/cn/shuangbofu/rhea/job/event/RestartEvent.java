package cn.shuangbofu.rhea.job.event;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by shuangbofu on 2020/11/1 下午3:23
 */
@Data
@AllArgsConstructor
public class RestartEvent implements Event {
    private Long actionId;
}
