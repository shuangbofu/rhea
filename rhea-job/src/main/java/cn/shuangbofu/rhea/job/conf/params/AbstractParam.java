package cn.shuangbofu.rhea.job.conf.params;

import java.lang.reflect.Field;

/**
 * Created by shuangbofu on 2020/10/31 10:59
 */
public abstract class AbstractParam implements Param {

    @Override
    public String get(String key) {
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getName().equals(key)) {
                try {
                    Object o = field.get(this);
                    if (o != null) {
                        return o.toString();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
