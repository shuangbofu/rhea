package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import lombok.Getter;


public enum Execution {
    /**
     *
     */
    SUBMIT(JobStatus.SUBMITTED),
    STOP(JobStatus.STOPPED),
    RUN(JobStatus.RUNNING),
    PUBLISH(JobStatus.PUBLISHED),
    RESTART(JobStatus.RUNNING),
    KILL;

    @Getter
    private JobStatus finalStatus;

    Execution() {
    }

    Execution(JobStatus finalStatus) {
        this.finalStatus = finalStatus;
    }

    public boolean isRestart() {
        return RESTART.equals(this);
    }
}