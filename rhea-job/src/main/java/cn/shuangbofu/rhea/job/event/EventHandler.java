package cn.shuangbofu.rhea.job.event;

import com.google.common.collect.Lists;

import java.util.HashSet;

/**
 * Created by shuangbofu on 2020/10/18 下午6:58
 */
public class EventHandler {
    private final HashSet<EventListener> listeners = new HashSet<>();

    public EventHandler() {
    }


    public void addListener(EventListener listener) {
        listeners.add(listener);
    }

    public void fireEventListeners(Event event) {
        for (EventListener listener : Lists.newArrayList(listeners)) {
            listener.handleEvent(event);
        }
    }

    public void removeListener(EventListener listener) {
        listeners.remove(listener);
    }
}
