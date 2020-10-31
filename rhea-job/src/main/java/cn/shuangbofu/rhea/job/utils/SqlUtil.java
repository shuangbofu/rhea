package cn.shuangbofu.rhea.job.utils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Created by shuangbofu on 2020-04-11 14:23
 */
public class SqlUtil {

    public static String fixSql(String sql, String region, String env) {
        // 修复kafka.group.id
        sql = fixLine(sql, "kafka.group.id", value -> {
            String prefix = "flink_";
            String suffix = "_test";
            if (!value.startsWith(prefix)) {
                value = prefix + value;
            }
            if (!value.endsWith(suffix) && "pre".equals(env)) {
                value += suffix;
            } else if (value.endsWith(suffix) && "pro".equals(env)) {
                int index = value.indexOf(suffix);
                value = value.substring(0, index);
            }
            return value;
        });

        // 修复时区
        sql = fixLine(sql, "timezone", value -> {
            if ("AY".equals(region)) {
                return "Asia/Shanghai";
            } else {
                return "Etc/GMT";
            }
        });
        return sql;
    }

    public static void main(String[] args) {
        String origin = "--udf函数声明\n" +
                "CREATE scala FUNCTION splitAndGetOne WITH com.tuya.tlink.udf.base.SplitAndGetOne;\n" +
                "CREATE scala FUNCTION parseRowTime WITH com.tuya.tlink.udf.base.ParseNginxTime;\n" +
                "CREATE TABLE nginxAccessLog(\n" +
                "    --kafka中的message所对应的字段名 \n" +
                "    --请修改这里 \n" +
                "    message varchar,\n" +
                "    splitAndGetOne(message, '#\\\\|#', 18) as errorCode,\n" +
                "    parseRowTime(splitAndGetOne(message, '#\\\\|#', 0)) as ctime,\n" +
                "    --设置watermark字段、延迟时间30秒 \n" +
                "    watermark for ctime as withoffset(ctime,30000)\n" +
                " )WITH(\n" +
                "    type = 'kafka10',\n" +
                "    kafka.bootstrap.servers = '${biz_kafka_url}',\n" +
                "    kafka.auto.offset.reset = 'latest',\n" +
                "    kafka.topic = '请修改这里',\n" +
                "    kafka.group.id = '请修改这里',\n" +
                "    sourcedatatype ='json',\n" +
                "    --source任务并行度 \n" +
                "    parallelism ='2',\n" +
                "    --时区字段,bling会自动检测并填入 \n" +
                "    timezone='Etc/GMT'\n" +
                " );\n" +
                "\n" +
                "-----华丽的分割线-----\n" +
                "--udf函数声明\n" +
                "CREATE scala FUNCTION splitAndGetOne WITH com.tuya.tlink.udf.base.SplitAndGetOne;\n" +
                "CREATE scala FUNCTION parseRowTime WITH com.tuya.tlink.udf.base.ParseNginxTime;\n" +
                "CREATE TABLE nginxAccessLog(\n" +
                "    --kafka中的message所对应的字段名 \n" +
                "    --请修改这里 \n" +
                "    message varchar,\n" +
                "    splitAndGetOne(message, '#\\\\|#', 18) as errorCode,\n" +
                "    parseRowTime(splitAndGetOne(message, '#\\\\|#', 0)) as ctime,\n" +
                "    --设置watermark字段、延迟时间30秒 \n" +
                "    watermark for ctime as withoffset(ctime,30000)\n" +
                " )WITH(\n" +
                "    type = 'kafka',\n" +
                "    kafka.bootstrap.servers = '${mock_kafka_url}',\n" +
                "    kafka.auto.offset.reset = 'latest',\n" +
                "    kafka.topic = 'john',\n" +
                "    kafka.group.id = 'bling',\n" +
                "    sourcedatatype ='json',\n" +
                "    --source任务并行度 \n" +
                "    parallelism ='2',\n" +
                "    --时区字段,bling会自动检测并填入 \n" +
                "    timezone='Etc/GMT'\n" +
                " );";

        String s = fixSql(origin, "EU", "pro");
    }

    private static String fixLine(String sql, String key, Function<String, String> function) {
        String regex = key + "( *)=( *)'(.+?)'";
        System.out.println(regex);
        Pattern pattern = compile(regex);
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            String line = matcher.group(0);
            System.out.println(line);
            int index = line.indexOf("'");
            String value = line.substring(index + 1, line.length() - 1);
            String newValue = function.apply(value);
            String newLine = line.replace(value, newValue);
            sql = sql.replace(line, newLine);
        }
        return sql;
    }
}
