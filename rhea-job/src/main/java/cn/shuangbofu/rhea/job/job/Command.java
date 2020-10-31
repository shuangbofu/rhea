package cn.shuangbofu.rhea.job.job;

public interface Command {
    String SUBMIT = "SUBMIT";
    String STOP = "STOP";
    String RUN = "RUN";
    String PUBLISH = "PUBLISH";
    String KILL = "KILL";
}