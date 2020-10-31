package cn.shuangbofu.rhea.job.job.shell;

import cn.shuangbofu.rhea.job.utils.Shell;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by shuangbofu on 2020/10/31 14:14
 */
public class CmdProcessTest extends TestCase {

    @Test
    public void test() throws Exception {
        Shell.execute("sh /tmp/schedule_sleep.sh");
//        Process exec = Runtime.getRuntime().exec("sh /tmp/schedule_sleep.sh");
    }
}
