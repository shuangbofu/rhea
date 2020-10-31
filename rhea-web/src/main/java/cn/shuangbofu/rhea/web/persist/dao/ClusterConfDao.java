package cn.shuangbofu.rhea.web.persist.dao;

import cn.shuangbofu.rhea.web.persist.entity.ClusterConf;

/**
 * Created by shuangbofu on 2020/10/30 下午12:01
 */
public class ClusterConfDao extends BaseDao<ClusterConf> {
    protected ClusterConfDao() {
        super(ClusterConf.class);
    }

    public ClusterConf findValidOneById(Long id) {
        return findOneBy(q -> q.where(ClusterConf::getValid, true).where(ClusterConf::getId, id));
    }
}
