package cn.shuangbofu.rhea.job.conf.params;

/**
 * Created by shuangbofu on 2020/10/30 下午12:59
 */
public class ClusterParam implements Param {

    public static final String USERNAME = "username";
    public static final String PORT = "port";
    public static final String HOST = "host";
    public static final String MASTER = "master";
    public static final String WORKERS = "workers";
    public static final String PRIVATE_KEY_PATH = "privateKeyPath";

    @Override
    public String name() {
        return "cluster";
    }

    @Override
    public String get(String key) {
        return null;
    }
}
