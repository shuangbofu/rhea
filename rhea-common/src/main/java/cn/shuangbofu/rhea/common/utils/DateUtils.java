package cn.shuangbofu.rhea.common.utils;

import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Date;

/**
 * Created by shuangbofu on 2020/11/1 下午2:15
 */
public class DateUtils {

    public static String now() {
        return DateFormatUtils.format(new Date(), "yyyy-MM-dd-HH-mm-ss");
    }
}
