package cn.shuangbofu.rhea.web.vo.param;

import lombok.Data;

/**
 * Created by shuangbofu on 2020/10/18 下午4:51
 */
@Data
public class JobSubmitParam {
    private Long actionId;
    /**
     * 其他提交时的参数
     */
    private Boolean stopCurrent = false;
}
