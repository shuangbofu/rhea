/*
 * Copyright 2012 LinkedIn Corp.
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

import cn.shuangbofu.rhea.job.JobLogger;
import com.google.common.base.Joiner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class LogCollector extends Thread {

    private final BufferedReader inputReader;
    private final JobLogger logger;
    private final CircularBuffer<String> buffer;
    private final Boolean error;

    public LogCollector(Reader inputReader, JobLogger logger,
                        boolean error, int bufferLines) {
        super("process-log");
        this.inputReader = new BufferedReader(inputReader);
        this.logger = logger;
        this.error = error;
        buffer = new CircularBuffer<>(bufferLines);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                String line = inputReader.readLine();
                if (line == null) {
                    return;
                }

                buffer.append(line);
                if (error) {
                    logger.error(line);
                } else {
                    logger.info(line);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading from logging stream:", e);
        }
    }

    public void awaitCompletion(long waitMs) {
        try {
            join(waitMs);
        } catch (InterruptedException e) {
            logger.info("I/O thread interrupted.", e);
        }
    }

    public String getRecentLog() {
        return Joiner.on(System.getProperty("line.separator")).join(buffer);
    }
}
