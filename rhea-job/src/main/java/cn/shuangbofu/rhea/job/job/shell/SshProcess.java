package cn.shuangbofu.rhea.job.job.shell;

import cn.shuangbofu.rhea.job.JobLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shuangbofu on 2020/10/30 18:04
 */
public class SshProcess implements IProcess {
    private final CmdProcess cmdProcess;
    private final String username;
    private final String host;
    private final Integer port;
    private final JobLogger logger;
    private final String privateKeyPath;

    public SshProcess(String username, String host, int port, String privateKeyPath, String cmd, JobLogger logger) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.privateKeyPath = privateKeyPath;
        this.logger = logger;
        List<String> cmds = new ArrayList<>();
        cmds.add("bash");
        cmds.add("-c");
        cmds.add(sshCmd(cmd));
        cmdProcess = new CmdProcess(cmds, logger);
    }

    @Override
    public void execute() {
        cmdProcess.execute();
    }

    @Override
    public void kill() {
        SshProcess killProcess = new SshProcess(username, host, port, privateKeyPath, sshCmd("kill -9 " + cmdProcess.getProcessId()), logger);
        killProcess.execute();
    }

    @Override
    public int getProcessId() {
        return cmdProcess.getProcessId();
    }

    private String sshCmd(String origin) {
        return String.format("ssh -i %s -o StrictHostKeyChecking=no %s@%s -p %s \"%s\"", privateKeyPath, username, host, port, origin);
    }
}
