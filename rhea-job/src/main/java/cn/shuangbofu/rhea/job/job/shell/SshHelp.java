package cn.shuangbofu.rhea.job.job.shell;

import cn.shuangbofu.rhea.job.JobLogger;

import java.util.function.Consumer;

/**
 * Created by shuangbofu on 2020/10/30 18:11
 */
public class SshHelp {
    private final String username;
    private final String host;
    private final int port;
    private final String privateKeyPath;
    private final JobLogger logger;

    public SshHelp(String username, String host, int port, String privateKeyPath, JobLogger logger) {
        this.username = username;
        this.host = host;
        this.port = port;
        this.logger = logger;
        this.privateKeyPath = privateKeyPath;
    }

    public SshProcess ssh(String cmd, Consumer<SshProcess> consumer) {
        SshProcess sshProcess = new SshProcess(username, host, port, privateKeyPath, cmd, logger);
        consumer.accept(sshProcess);
        sshProcess.execute();
        return sshProcess;
    }

    public SshProcess scp(String localPath, String remotePath, Consumer<SshProcess> consumer) {
        return ssh(getScpCmd(localPath, remotePath), consumer);
    }

    private String getScpCmd(String localPath, String remotePath) {
        return String.format("scp %s %s@%s:%s", localPath, username, host, remotePath);
    }

    public String getUsername() {
        return username;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
