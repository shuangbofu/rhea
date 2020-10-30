package cn.shuangbofu.rhea.job.job;

import cn.shuangbofu.rhea.common.utils.FileUtil;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by shuangbofu on 2020-04-04 02:25
 */
public class FileLogger implements cn.shuangbofu.rhea.job.JobLogger {

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
        info("log init success");
    }


    private String format(String s, Object... args) {
        return String.format(s.replace("{}", "%s"), args);
    }

    public void info(String s, Throwable t, Object... args) {
        logger.info(format(s, args), t);
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public void info(String s, Object... args) {
        logger.info(format(s, args));
    }

    @Override
    public void error(String s, Throwable t, Object... args) {
        logger.error(format(s, args), t);
    }

    @Override
    public void error(String s, Object... args) {
        logger.error(format(s, args));
    }

    public void warn(String s, Throwable t, Object... args) {
        logger.warn(format(s, args), t);
    }

    public void warn(String s, Object... args) {
        logger.warn(format(s, args));
    }

    public FileUtil.LogData getLog(int offset, int length) throws IOException {
        if (logFile != null) {
            File file = new File(logFile.getParent(), logFile.getName());
            return FileUtil.readUtf8File(file, offset, length);
        }
        throw new RuntimeException("log not exist");
    }

    @Override
    public void close() {
        info("关闭日志({})", name);
        logger.removeAllAppenders();
        fileAppender.close();
        consoleAppender.close();
        logger = null;
        uploadLogFile(logFile);
    }

    /**
     * copy from azkaban
     *
     * @param files
     */
    private void uploadLogFile(File... files) {
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
                            // TODO
                            // Flush here.
//                            LogManager.saveLog(new LogModel().setType(type).setRefId(id).setAttempt(attempt)
//                                    .setLog(buffer)
//                                    .setStartByte(startByte)
//                                    .setEndByte(startByte + buffer.length));
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
//                LogManager.saveLog(new LogModel().setType(type).setRefId(id).setAttempt(attempt)
//                        .setLog(Arrays.copyOf(buffer, pos))
//                        .setStartByte(startByte).setEndByte(startByte + pos));
            }
        } catch (IOException e) {
        }
    }
}
