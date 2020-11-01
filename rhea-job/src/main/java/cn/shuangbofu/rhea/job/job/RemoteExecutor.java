package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.job.JobLogger;
import cn.shuangbofu.rhea.job.conf.params.ParamStore;
import cn.shuangbofu.rhea.job.job.shell.IProcess;
import cn.shuangbofu.rhea.job.job.shell.SshHelp;
import cn.shuangbofu.rhea.job.utils.Shell;
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
    private final Object lock = new Object();
    private final JobLogger logger;
    private IProcess process;

    public RemoteExecutor(ParamStore store, JobLogger logger) {
        this.logger = logger;
        List<String> workers = store.getListFromString("workers", ",");
        String masterHost = store.getValue("host");
        int port = store.getIntValue("port");
        String username = store.getValue("username");
        String privateKeyPath = store.getValue("privateKeyPath");

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

    public void createFile2Remote(String content, String remotePath, boolean all) {
        try {
            String localPath = "/tmp/tmp_copy_file/" + System.currentTimeMillis();
            FileUtils.writeStringToFile(new File(localPath), content, "UTF-8");
            scp(localPath, remotePath, all);
            Shell.execute("rm " + localPath, logger);
        } catch (IOException e) {
            throw new RuntimeException("write file error");
        }
    }

    public void cancel() {
        synchronized (lock) {
            logger.info("get:{}", process);
            if (process != null) {
                process.kill();
                process = null;
            }
        }
    }

    private void setCurrent(IProcess process) {
        synchronized (lock) {
            this.process = process;
            logger.info("set: {}", process);
        }
    }

    public void local(String cmd) {
        Shell.execute(cmd, logger, this::setCurrent);
    }
}
