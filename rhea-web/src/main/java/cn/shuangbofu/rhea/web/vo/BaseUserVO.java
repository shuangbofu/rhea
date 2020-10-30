package cn.shuangbofu.rhea.web.vo;

import cn.shuangbofu.rhea.web.User;
import cn.shuangbofu.rhea.web.UserCache;
import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/18 上午11:16
 */
@Data
public abstract class BaseUserVO<T> {
    protected Long gmtCreate;
    protected Long gmtModified;

    protected String createUser;
    protected String modifyUser;

    public T setGmtCreate(Long gmtCreate) {
        this.gmtCreate = gmtCreate;
        return (T) this;
    }

    public T setGmtModified(Long gmtModified) {
        this.gmtModified = gmtModified;
        return (T) this;
    }

    public T setCreateUser(String createUser) {
        this.createUser = createUser;
        return (T) this;
    }

    public T setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
        return (T) this;
    }

    public User getCreateUser() {
        return UserCache.getUser(createUser);
    }

    public User getModifyUser() {
        return UserCache.getUser(modifyUser);
    }
}
