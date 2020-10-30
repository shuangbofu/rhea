/*
 * Copyright 2017 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package cn.shuangbofu.rhea.job.job.shell;

import cn.shuangbofu.rhea.common.tuple.TwoTuple;
import cn.shuangbofu.rhea.job.JobLogger;
import com.google.common.base.Joiner;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * An improved version of java.lang.Process.
 * <p>
 * Output is read by separate threads to avoid deadlock and logged to log4j loggers.
 */
public class CmdProcess implements IProcess {

    public static String KILL_COMMAND = "cancel";

    private final List<String> cmd;
    private final CountDownLatch startupLatch;
    private final CountDownLatch completeLatch;
    private final JobLogger logger;
    private volatile int processId;
    private volatile Process process;
    private TwoTuple<String, String> logTuple;

    public CmdProcess(List<String> cmd, JobLogger logger) {
        this.cmd = cmd;
        processId = -1;
        startupLatch = new CountDownLatch(1);
        completeLatch = new CountDownLatch(1);
        this.logger = logger;
    }

    /**
     * Execute this process, blocking until it has completed.
     */
    public void run() throws IOException {
        if (isStarted() || isComplete()) {
            throw new IllegalStateException("The process can only be used once.");
        }

        logger.info("执行运行任务脚本 {}", cmd.toString());
        ProcessBuilder builder = new ProcessBuilder(cmd);

        builder.redirectErrorStream(true);
        process = builder.start();

        try {
            processId = processId(process);
            if (processId == 0) {
                logger.info("Spawned thread with unknown process id");
            } else {
                logger.info("Spawned thread with process id " + processId);
            }

            startupLatch.countDown();

            LogCollector outputGobbler =
                    new LogCollector(
                            new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8),
                            logger, false, 30);
            LogCollector errorGobbler =
                    new LogCollector(
                            new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8),
                            logger, true, 30);

            outputGobbler.start();
            errorGobbler.start();
            int exitCode = -1;
            try {
                exitCode = process.waitFor();
            } catch (InterruptedException e) {
                logger.info("Process interrupted. Exit code is " + exitCode, e);
            }

            completeLatch.countDown();

            // try to wait for everything to get logged out before exiting
            outputGobbler.awaitCompletion(5000);
            errorGobbler.awaitCompletion(5000);
            logTuple = new TwoTuple<>(outputGobbler.getRecentLog(), errorGobbler.getRecentLog());

            if (exitCode != 0) {
                String output = "正常日志:\n" + outputGobbler.getRecentLog() + "\n" + "异常日志:\n" + errorGobbler.getRecentLog();
                throw new RuntimeException(output);
//                throw new ProcessFailureException(exitCode, output);
            }

        } finally {
            IOUtils.closeQuietly(process.getInputStream());
            IOUtils.closeQuietly(process.getOutputStream());
            IOUtils.closeQuietly(process.getErrorStream());
        }
    }

    @Override
    public void execute() {
        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("execute error");
        }
    }

    @Override
    public void kill() {
        hardKill();
    }

    public TwoTuple<String, String> getLog() {
        if (isComplete()) {
            return logTuple;
        }
        return null;
    }

    public void run2() throws IOException {
        run();
    }

    /**
     * Await the completion of this process
     *
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public void awaitCompletion() throws InterruptedException {
        completeLatch.await();
    }

    /**
     * Await the start of this process
     * <p>
     * When this method returns, the job process has been created and a this.processId has been set.
     *
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public void awaitStartup() throws InterruptedException {
        startupLatch.await();
    }

    /**
     * Get the process id for this process, if it has started.
     *
     * @return The process id or -1 if it cannot be fetched
     */
    @Override
    public int getProcessId() {
        checkStarted();
        return processId;
    }

    /**
     * Attempt to cancel the process, waiting up to the given time for it to die
     *
     * @param time The amount of time to wait
     * @param unit The time unit
     * @return true iff this soft cancel kills the process in the given wait time.
     */
    public boolean softKill(long time, TimeUnit unit)
            throws InterruptedException {
        checkStarted();
        if (processId != 0 && isStarted()) {
            try {
                String cmd = String.format("%s %d", KILL_COMMAND, processId);
                Runtime.getRuntime().exec(cmd);
                return completeLatch.await(time, unit);
            } catch (IOException e) {
                logger.error("Kill attempt failed.", e);
            }
            return false;
        }
        return false;
    }

    /**
     * Force cancel this process
     */
    public void hardKill() {
        checkStarted();
        if (isRunning()) {
            if (processId != 0) {
                try {
                    String cmd = String.format("%s -9 %d", KILL_COMMAND, processId);
                    Runtime.getRuntime().exec(cmd);
                } catch (IOException e) {
                    logger.error("Kill attempt failed.", e);
                }
            }
            process.destroy();
        }
    }

    /**
     * Attempt to get the process id for this process
     *
     * @param process The process to get the id from
     * @return The id of the process
     */
    private int processId(Process process) {
        int processId = 0;
        try {
            Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);

            processId = f.getInt(process);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return processId;
    }

    /**
     * @return true iff the process has been started
     */
    public boolean isStarted() {
        return startupLatch.getCount() == 0L;
    }

    /**
     * @return true iff the process has completed
     */
    public boolean isComplete() {
        return completeLatch.getCount() == 0L;
    }

    /**
     * @return true iff the process is currently running
     */
    public boolean isRunning() {
        return isStarted() && !isComplete();
    }

    public void checkStarted() {
        if (!isStarted()) {
            throw new IllegalStateException("Process has not yet started.");
        }
    }

    @Override
    public String toString() {
        return "Process(cmd = " + Joiner.on(" ").join(cmd);
    }
}
