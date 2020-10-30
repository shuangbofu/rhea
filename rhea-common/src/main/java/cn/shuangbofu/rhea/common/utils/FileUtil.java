package cn.shuangbofu.rhea.common.utils;

import cn.shuangbofu.rhea.common.tuple.TwoTuple;
import lombok.Data;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by shuangbofu on 2020-04-04 13:46
 */
public class FileUtil {

    public static LogData readUtf8File(File file, int fileOffset, int length)
            throws IOException {
        byte[] buffer = new byte[length];
        FileInputStream fileStream = new FileInputStream(file);

        long skipped = fileStream.skip(fileOffset);
        if (skipped < fileOffset) {
            fileStream.close();
            return new LogData(fileOffset, 0, "");
        }

        BufferedInputStream inputStream = null;
        int read = 0;
        try {
            inputStream = new BufferedInputStream(fileStream);
            read = inputStream.read(buffer);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        if (read <= 0) {
            return new LogData(fileOffset, 0, "");
        }
        TwoTuple<Integer, Integer> utf8Range = getUtf8Range(buffer, 0, read);
        String outputString =
                new String(buffer, utf8Range.first, utf8Range.second, StandardCharsets.UTF_8);

        return new LogData(fileOffset + utf8Range.first, utf8Range.second, outputString);
    }

    /**
     * Returns first and length.
     */
    public static TwoTuple<Integer, Integer> getUtf8Range(byte[] buffer, int offset,
                                                          int length) {
        int start = getUtf8ByteStart(buffer, offset);
        int end = getUtf8ByteEnd(buffer, offset + length - 1);

        return new TwoTuple<>(start, end - start + 1);
    }

    private static int getUtf8ByteStart(byte[] buffer, int offset) {
        // If it's a proper utf-8, we should find it within the next 6 bytes.
        for (int i = offset; i < offset + 6 && i < buffer.length; i++) {
            byte b = buffer[i];
            // check the mask 0x80 is 0, which is a proper ascii
            if ((0x80 & b) == 0) {
                return i;
            } else if ((0xC0 & b) == 0xC0) {
                return i;
            }
        }

        // Don't know what it is, will just set it as 0
        return offset;
    }

    private static int getUtf8ByteEnd(byte[] buffer, int offset) {
        // If it's a proper utf-8, we should find it within the previous 12 bytes.
        for (int i = offset; i > offset - 11 && i >= 0; i--) {
            byte b = buffer[i];
            // check the mask 0x80 is 0, which is a proper ascii. Just return
            if ((0x80 & b) == 0) {
                return i;
            }

            if ((b & 0xE0) == 0xC0) { // two byte utf8 char. bits 110x xxxx
                if (offset - i >= 1) {
                    // There is 1 following byte we're good.
                    return i + 1;
                }
            } else if ((b & 0xF0) == 0xE0) { // three byte utf8 char. bits 1110 xxxx
                if (offset - i >= 2) {
                    // There is 1 following byte we're good.
                    return i + 2;
                }
            } else if ((b & 0xF8) == 0xF0) { // four byte utf8 char. bits 1111 0xxx
                if (offset - i >= 3) {
                    // There is 1 following byte we're good.
                    return i + 3;
                }
            } else if ((b & 0xFC) >= 0xF8) { // five byte utf8 char. bits 1111 10xx
                if (offset - i == 4) {
                    // There is 1 following byte we're good.
                    return i + 4;
                }
            } else if ((b & 0xFE) == 0xFC) { // six byte utf8 char. bits 1111 110x
                if (offset - i >= 5) {
                    // There is 1 following byte we're good.
                    return i + 5;
                }
            }
        }

        // Don't know what it is, will just set it as 0
        return offset;
    }

    @Data
    public static class LogData {
        private int offset;
        private int length;
        private String value;

        public LogData(int offset, int length, String value) {
            this.offset = offset;
            this.length = length;
            this.value = value;
        }
    }
}
