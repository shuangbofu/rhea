package cn.shuangbofu.rhea.job.conf.params;

import cn.shuangbofu.rhea.common.utils.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by shuangbofu on 2020/10/30 17:16
 */
public class ParamStore {
    private final Map<String, Param> params;

    public ParamStore() {
        params = Maps.newConcurrentMap();
    }

    public void addParam(Param param) {
        params.put(param.name(), param);
    }

    public void addParams(List<Param> list) {
        list.forEach(this::addParam);
    }

    public String getValue(String key) {
        for (Param param : params.values()) {
            String s = param.get(key);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    public List<String> getListFromString(String key, String split) {
        String value = getValue(key);
        String[] strings = value.split(split);
        return Lists.newArrayList(Arrays.asList(strings).stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList()));
    }

    public int getIntValue(String key) {
        String value = getValue(key);
        return Integer.parseInt(value);
    }
}
