package cn.shuangbofu.rhea.job.conf.params;

import cn.shuangbofu.rhea.common.utils.StringUtils;
import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/30 下午12:59
 */
@Data
public class ClusterParam extends AbstractParam {
    private String username;
    private String master;
    private String workers;
    private String privateKeyPath;
    private String rsAddress;
    private String hdfsAddresses;

    @Override
    public String name() {
        return "cluster";
    }

    @Override
    public String get(String key) {
        String s = super.get(key);
        if (s != null) {
            return s;
        }
        if ("port".equals(key) || "host".equals(key)) {
            String master = super.get("master");
            if (StringUtils.isNotEmpty(master)) {
                String[] split = master.split(":");
                if (split.length > 1 && "port".equals(key)) {
                    return split[1];
                }
                if (split.length > 0 && "host".equals(key)) {
                    return split[0];
                }
            }
        }
        return null;
    }
}
