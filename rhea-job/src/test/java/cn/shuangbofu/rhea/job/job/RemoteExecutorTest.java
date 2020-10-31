package cn.shuangbofu.rhea.job.job;


import cn.shuangbofu.rhea.job.conf.params.Param;
import cn.shuangbofu.rhea.job.conf.params.ParamStore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by shuangbofu on 2020/10/30 19:23
 */
public class RemoteExecutorTest {
    private String scheduledShell(int count) {
        return "#!/bin/bash  \n" +
                "  \n" +
                "for((i=1;i<=" + count + ";i++));  \n" +
                "do   \n" +
                "echo $(expr $i \\* 1 + 0);  \n" +
                "sleep 1s \n" +
                "done  ";
    }

    @Test
    public void test() {
        ParamStore paramStore = new ParamStore();
        paramStore.addParam(
                new TestParam(5)
                        .set("username", "shuangbofu")
                        .set("host", "127.0.0.1")
                        .set("port", 22)
                        .set("workers", "")
                        .set("privateKeyPath", "/Users/shuangbofu/.ssh/id_rsa")
        );
        RemoteExecutor remoteExecutor = new RemoteExecutor(paramStore, new FileLogger("john", "test_rhea", false));
        String shellPath = "/tmp/schedule_sleep.sh";
        remoteExecutor.createFile2Remote(scheduledShell(120), shellPath, false);
//        remoteExecutor.ssh("ls -Slh", false);
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                remoteExecutor.cancel();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        remoteExecutor.local("sh " + shellPath);
//        remoteExecutor.ssh("sh " + shellPath, false);
    }

    static class TestParam implements Param {

        private final Map<String, Object> map;

        public TestParam(int size) {
            map = new HashMap<>(size);
        }

        @Override
        public String name() {
            return "test";
        }

        @Override
        public String get(String key) {
            Object o = map.get(key);
            if (o == null) {
                return null;
            }
            return o.toString();
        }

        public TestParam set(String key, Object value) {
            map.put(key, value);
            return this;
        }
    }
}
