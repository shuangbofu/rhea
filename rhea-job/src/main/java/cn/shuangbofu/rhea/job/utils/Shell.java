package cn.shuangbofu.rhea.job.utils;

import cn.shuangbofu.rhea.job.JobLogger;
import cn.shuangbofu.rhea.job.job.FileLogger;
import cn.shuangbofu.rhea.job.job.shell.CmdProcess;
import cn.shuangbofu.rhea.job.job.shell.ProcessFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by shuangbofu on 2020/10/31 15:18
 */
public class Shell {

    public static CmdProcess execute(String cmd, JobLogger logger, Consumer<CmdProcess> consumer) {
        boolean closeLogger = logger == null;
        if (closeLogger) {
            logger = new FileLogger(System.currentTimeMillis() + "", "local_command_execute_tmp", false);
        }
        List<String> cmds = new ArrayList<>();
        cmds.add("bash");
        cmds.add("-c");
        cmds.add(cmd);
        CmdProcess cmdProcess = new CmdProcess(cmds, logger);
        consumer.accept(cmdProcess);
        try {
            cmdProcess.execute();
        } catch (Exception e) {
            if (closeLogger) {
                if (e instanceof ProcessFailureException) {
                    ProcessFailureException processFailureException = (ProcessFailureException) e;
                    logger.error(processFailureException.toString());
                } else {
                    logger.error("error", e);
                }
            } else {
                throw e;
            }
        } finally {
            if (closeLogger) {
                logger.close();
            }
        }
        return cmdProcess;
    }

    public static CmdProcess execute(String cmd, JobLogger logger) {
        return execute(cmd, logger, p -> {
        });
    }

    public static CmdProcess execute(String cmd, Consumer<CmdProcess> consumer) {
        return execute(cmd, null, consumer);
    }

    public static CmdProcess execute(String cmd) {
        return execute(cmd, p -> {
        });
    }
}
