package cn.shuangbofu.rhea.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogData {
    private String key;
    private int offset;
    private int length;
    private String value;
    private long timestamp;

    public LogData(int offset, int length, String value) {
        this.offset = offset;
        this.length = length;
        this.value = value;
    }
}
