package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.job.JobLogger;
import cn.shuangbofu.rhea.job.conf.params.ClusterParam;
import cn.shuangbofu.rhea.job.conf.params.ParamStore;
import cn.shuangbofu.rhea.job.job.shell.IProcess;
import cn.shuangbofu.rhea.job.job.shell.SshHelp;
import cn.shuangbofu.rhea.job.job.shell.SshProcess;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by shuangbofu on 2020/10/30 16:59
 */
public class RemoteExecutor {

    private final List<SshHelp> workerSshHelps = Lists.newArrayList();
    private final SshHelp masterSshHelp;
    private final JobLogger logger;
    private final Object lock = new Object();
    private IProcess currentProcess;

    public RemoteExecutor(ParamStore store, JobLogger logger) {
        this.logger = logger;
        List<String> workers = store.getListFromString(ClusterParam.WORKERS, ",");
        String masterHost = store.getValue(ClusterParam.HOST);
        int port = store.getIntValue(ClusterParam.PORT);
        String username = store.getValue(ClusterParam.USERNAME);
        String privateKeyPath = store.getValue(ClusterParam.PRIVATE_KEY_PATH);

        workers.forEach(worker -> workerSshHelps.add(new SshHelp(username, worker, port, privateKeyPath, logger)));
        masterSshHelp = new SshHelp(username, masterHost, port, privateKeyPath, logger);
    }

    public void ssh(String cmd, boolean all) {
        masterSshHelp.ssh(cmd, this::setCurrent);
        if (all) {
            workerSshHelps.forEach(sshHelp -> sshHelp.ssh(cmd, this::setCurrent));
        }
    }

    public void scp(String localPath, String remotePath, boolean all) {
        masterSshHelp.scp(localPath, remotePath, this::setCurrent);
        if (all) {
            workerSshHelps.forEach(sshHelp -> sshHelp.scp(localPath, remotePath, this::setCurrent));
        }
    }

    public void createFile2Remote(String content, String name, String remotePath, boolean all) {
        try {
            String localPath = "/tmp/" + name;
            FileUtils.writeStringToFile(new File(localPath), content, "UTF-8");
            scp(localPath, remotePath, all);
        } catch (IOException e) {
            throw new RuntimeException("write file error");
        }
    }

    public void cancel() {
        synchronized (lock) {
            if (currentProcess != null) {
                logger.info("取消执行，进程号：" + currentProcess.getProcessId());
                currentProcess.kill();
            }
        }
    }

    private void setCurrent(SshProcess sshProcess) {
        synchronized (lock) {
            currentProcess = sshProcess;
        }
    }
}
