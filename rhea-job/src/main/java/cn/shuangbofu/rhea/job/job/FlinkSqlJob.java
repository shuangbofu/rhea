package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.enums.JobStatus;
import cn.shuangbofu.rhea.common.tuple.ThreeTuple;
import cn.shuangbofu.rhea.job.conf.JobActionProcess;
import cn.shuangbofu.rhea.job.conf.JobConf;
import cn.shuangbofu.rhea.job.conf.JobText;
import cn.shuangbofu.rhea.job.conf.params.ParamStore;
import cn.shuangbofu.rhea.job.utils.SqlUtil;

/**
 * Created by shuangbofu on 2020/10/18 下午7:10
 */
public class FlinkSqlJob extends FlinkJob {
    public FlinkSqlJob(Long jobId, String jobName, Long actionId, JobStatus jobStatus, JobText text, JobConf conf, JobActionProcess result) {
        super(jobId, jobName, actionId, jobStatus, text, conf, result);
    }

    @Override
    public void publish() {
        super.publish();
//        runner.getExecutor().createFile2Remote(getSql(), getPath(false).first, false);
//        runner.getExecutor().createFile2Remote("sh ");
    }

    private String getSql() {
        ParamStore paramStore = runner.getParamStore();
        String env = paramStore.getValue("env");
        String region = paramStore.getValue("region");
        return SqlUtil.fixSql(String.join("\n", text.getSources()), region, env) +
                String.join("\n", text.getSides()) +
                String.join("\n", text.getSinks()) +
                String.join("\n", text.getSqls());
    }

    /**
     * sql文本，conf配置文件，script脚本
     *
     * @param formal
     * @return
     */
    private ThreeTuple<String, String, String> getPath(boolean formal) {
        String folder = formal ? "/formal" : "/publish";

        // /home/{username}/{folder}/{jobName}_{actionId}/xxx.xx
        String basePath = getRootPath() + folder + "/" + jobName + "_" + actionId + "/";
        return new ThreeTuple<>(basePath + "text.sql", basePath + "conf.json", basePath + "script.sh");
    }
}
