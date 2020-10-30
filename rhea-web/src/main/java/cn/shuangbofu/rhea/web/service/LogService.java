package cn.shuangbofu.rhea.web.service;

import cn.shuangbofu.rhea.job.event.Event;
import cn.shuangbofu.rhea.job.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Created by shuangbofu on 2020/10/18 下午7:24
 */
@Service
public class LogService implements EventListener {

    @Override
    public void handleEvent(Event event) {

    }
}
