package cn.shuangbofu.rhea.job.conf;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by shuangbofu on 2020/10/18 上午11:35
 */
@Data
@Accessors(chain = true)
public class JobText {
    List<String> sqls;
    List<String> sources;
    List<String> sides;
    List<String> sinks;

    public static JobText defaultValue() {
        List<String> empty = Lists.newArrayList();
        return new JobText().setSqls(empty).setSides(empty).setSources(empty).setSinks(empty);
    }
}
