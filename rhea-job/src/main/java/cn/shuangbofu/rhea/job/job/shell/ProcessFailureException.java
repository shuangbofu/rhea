package cn.shuangbofu.rhea.job.job.shell;

import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/31 11:33
 */
@Data
public class ProcessFailureException extends RuntimeException {

    private final int exitCode;
    private final String output;

    public ProcessFailureException(int exitCode, String output) {
        super("shell execute error");
        this.exitCode = exitCode;
        this.output = output;
    }

    @Override
    public String toString() {
        return "exitCode=" + exitCode +
                "\noutput='" + output + '\n';

    }
}
