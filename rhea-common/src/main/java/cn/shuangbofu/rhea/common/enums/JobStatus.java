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

    // 运行中
    RUNNING,

    // 执行中（启动中，发布中）
    EXECUTING,

    // 等待
    PENDING,

    // 发布完成
    PUBLISHED,

    // 提交完成
    SUBMITTED
}
