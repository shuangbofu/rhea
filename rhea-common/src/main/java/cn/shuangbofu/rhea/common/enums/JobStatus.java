package cn.shuangbofu.rhea.common.enums;

import lombok.AllArgsConstructor;

/**
 * Created by shuangbofu on 2020/10/18 下午1:16
 */
@AllArgsConstructor
public enum JobStatus {
    /**
     * 任务状态enum
     */
    FAILED,
    STOPPED,
    RUNNING,
    LAUNCHING,
    PENDING,
    ;
}
