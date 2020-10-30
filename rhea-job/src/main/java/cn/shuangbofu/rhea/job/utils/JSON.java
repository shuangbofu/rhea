package cn.shuangbofu.rhea.job.utils;

import cn.shuangbofu.rhea.job.conf.FlinkConf;
import cn.shuangbofu.rhea.job.conf.FlinkSqlConf;
import cn.shuangbofu.rhea.job.conf.JobConf;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import java.io.IOException;
import java.util.List;

/**
 * Created by shuangbofu on 2020/8/8 下午11:20
 */
public class JSON {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        setupMapper(MAPPER);
    }

    public static ObjectMapper setupMapper(ObjectMapper mapper) {
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
//            .enable(SerializationFeature.INDENT_OUTPUT)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

        mapper.registerSubtypes(new NamedType(JobConf.class));
        mapper.registerSubtypes(new NamedType(FlinkSqlConf.class, JobConf.TYPE_FLINK_SQL));
        mapper.registerSubtypes(new NamedType(FlinkConf.class, JobConf.TYPE_FLINK));
        return mapper;
    }

    public static <T> T parseObject(String json, Class<T> tClass) {
        try {
            return MAPPER.readValue(json, tClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("parse json error");
        }
    }

    public static <T> T parseObject(Object fromValue, Class<T> tClass) {
        return MAPPER.convertValue(fromValue, tClass);
    }

    public static JsonNode object2JsonNode(Object fromValue) {
        return parseObject(fromValue, JsonNode.class);
    }

    public static JsonNode jsonString2JsonNode(String jsonString) {
        return parseObject(jsonString, JsonNode.class);
    }

    public static <T> List<T> parseArray(String json, Class<T> tClass) {
        try {
            return MAPPER.readValue(json, getJavaType(tClass));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("parse json 2 array error");
        }
    }

    public static <T> List<T> parseArray(Object fromValue, Class<T> tClass) {
        return MAPPER.convertValue(fromValue, getJavaType(tClass));
    }

    public static String toJSONString(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("bean to json error");
        }
    }

    private static <T> JavaType getJavaType(Class<T> tClass) {
        return MAPPER.getTypeFactory()
                .constructCollectionType(List.class, tClass);
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
