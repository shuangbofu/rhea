package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.LogData;
import cn.shuangbofu.rhea.common.utils.FileUtil;
import cn.shuangbofu.rhea.job.JobLogger;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shuangbofu on 2020-04-04 02:25
 */
public class FileLogger implements JobLogger {

    private static final Layout DEFAULT_LAYOUT = new EnhancedPatternLayout(
            "%d %t - [%p] - %m\n");
    private static final String DIR = "./logs/";
    @Getter
    private final File logFile;
    private final String name;
    private Appender fileAppender;
    private Logger logger;
    private Appender consoleAppender;

    public FileLogger(String name, String folder, boolean append) {
        this.name = name;
        Logger logger = Logger.getLogger(name);
        logFile = new File(DIR + folder + "/", name + ".log");
        try {
            fileAppender = new FileAppender(DEFAULT_LAYOUT, logFile.getAbsolutePath(), append);
            logger.addAppender(fileAppender);
            consoleAppender = new ConsoleAppender(DEFAULT_LAYOUT, ConsoleAppender.SYSTEM_OUT);
            logger.addAppender(consoleAppender);
        } catch (IOException e) {

        }
        this.logger = logger;
        info("log init success");
    }

    public static List<FileUtil.LogResult> getRes(File... files) {
        List<FileUtil.LogResult> logResults = Lists.newArrayList();
        byte[] buffer = new byte[50 * 1024];
        int pos = 0;
        int length = buffer.length;
        int startByte = 0;
        try {
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];

                BufferedInputStream bufferedStream =
                        new BufferedInputStream(new FileInputStream(file));
                try {
                    int size = bufferedStream.read(buffer, pos, length);
                    while (size >= 0) {
                        if (pos + size == buffer.length) {
                            logResults.add(new FileUtil.LogResult(Arrays.copyOf(buffer, buffer.length), startByte, startByte + buffer.length));
                            pos = 0;
                            length = buffer.length;
                            startByte += buffer.length;
                        } else {
                            // Usually end of file.
                            pos += size;
                            length = buffer.length - pos;
                        }
                        size = bufferedStream.read(buffer, pos, length);
                    }
                } finally {
                    IOUtils.closeQuietly(bufferedStream);
                }
            }

            // Final commit of buffer.
            if (pos > 0) {
                logResults.add(new FileUtil.LogResult(Arrays.copyOf(buffer, pos), startByte, startByte + pos));
            }
        } catch (IOException e) {
        }
        return logResults;
    }

    private String format(String s, Object... args) {
        if (args != null && args.length > 0) {
            return String.format(s.replace("{}", "%s"), args);
        }
        return s;
    }

    @Override
    public void info(String s, Throwable t, Object... args) {
        logger.info(format(s, args), t);
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public void info(String s, Object... args) {
        logger.info(format(s, args));
    }
//
//    public void warn(String s, Throwable t, Object... args) {
//        logger.warn(format(s, args), t);
//    }
//
//    public void warn(String s, Object... args) {
//        logger.warn(format(s, args));
//    }

    @Override
    public void error(String s, Throwable t, Object... args) {
        logger.error(format(s, args), t);
    }

    @Override
    public void error(String s, Object... args) {
        logger.error(format(s, args));
    }

    @Override
    public LogData getLog(int offset, int length) {
        if (logFile != null) {
            File file = new File(logFile.getParent(), logFile.getName());
            try {
                return FileUtil.readUtf8File(file, offset, length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("log not exist");
    }

    @Override
    public List<FileUtil.LogResult> close() {
        info("close log {}{}.log", DIR, name);
        logger.removeAllAppenders();
        fileAppender.close();
        consoleAppender.close();
        logger = null;
        return getRes(logFile);
    }


}
