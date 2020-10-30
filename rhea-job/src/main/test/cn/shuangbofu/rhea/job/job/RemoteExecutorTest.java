package cn.shuangbofu.rhea.job.job;


import cn.shuangbofu.rhea.job.conf.params.Param;
import cn.shuangbofu.rhea.job.conf.params.ParamStore;
import org.junit.Test;

/**
 * Created by shuangbofu on 2020/10/30 19:23
 */
class RemoteExecutorTest {

    public static void main(String[] args) {
        ParamStore paramStore = new ParamStore();
        paramStore.addParam(new Param() {
            @Override
            public String name() {
                return "cluster";
            }

            @Override
            public String get(String key) {
                if (key.equals("username")) {
                    return "shuangbofu";
                } else if (key.equals("host")) {
                    return "127.0.0.1";
                } else if (key.equals("port")) {
                    return "22";
                } else if (key.equals("workers")) {
                    return "";
                } else if (key.equals("privateKeyPath")) {
                    return "/Users/shuangbofu/.ssh/id_rsa";
                }
                return null;
            }
        });

        RemoteExecutor remoteExecutor = new RemoteExecutor(paramStore, new FileLogger("john", "test_rhea", false));

        String shellPath = "/tmp/schedule_sleep.sh";
        remoteExecutor.createFile2Remote(scheduledShell(30), "schedule_sleep", shellPath, false);
        remoteExecutor.ssh("ls -Slh", false);
        new Thread(() -> {
            try {
                Thread.sleep(10000);
                remoteExecutor.cancel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        remoteExecutor.ssh("sh " + shellPath, false);
    }

    private static String scheduledShell(int count) {
        return "#!/bin/bash  \n" +
                "  \n" +
                "for((i=1;i<=" + count + ";i++));  \n" +
                "do   \n" +
                "echo $(expr $i \\* 3 + 1);  \n" +
                "sleep 1s \n" +
                "done  ";
    }

    @Test
    public void test() {
        System.out.println("hello");
    }
}
