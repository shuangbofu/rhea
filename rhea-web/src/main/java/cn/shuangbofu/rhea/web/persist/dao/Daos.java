package cn.shuangbofu.rhea.web.persist.dao;

import cn.shuangbofu.rhea.web.persist.entity.Model;
import com.google.common.collect.Maps;
import io.github.biezhi.anima.Anima;

import java.util.Map;

/**
 * Created by shuangbofu on 2020/10/17 17:59
 */
public class Daos {

    private static final Map<Class<? extends BaseDao<?>>, BaseDao> CACHES = Maps.newConcurrentMap();

    private static <T extends BaseDao<R>, R extends Model<R>> T create(Class<T> tClass) {
        try {
            return tClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T extends BaseDao<R>, R extends Model<R>> T get(Class<T> tClass) {
        return (T) CACHES.computeIfAbsent(tClass, i -> create(tClass));
    }

    public static JobDao job() {
        return get(JobDao.class);
    }

    public static JobDetailDao jobDetail() {
        return get(JobDetailDao.class);
    }

    public static JobActionDao jobAction() {
        return get(JobActionDao.class);
    }

    public static ClusterConfDao clusterConf() {
        return get(ClusterConfDao.class);
    }

    public static ComponentConfDao componentConf() {
        return get(ComponentConfDao.class);
    }

    public static void atomic(Runnable runnable, String errMsg) {
        Anima.atomic(runnable).catchException(e -> {
            throw new RuntimeException(errMsg, e);
        });
    }
}
