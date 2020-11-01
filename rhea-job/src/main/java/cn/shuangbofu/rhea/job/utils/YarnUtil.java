package cn.shuangbofu.rhea.job.utils;

import com.google.common.collect.Lists;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * Created by shuangbofu on 2020-04-04 01:02
 */
public class YarnUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(YarnUtil.class);
    private static final Map<String, YarnClient> CLIENT_CACHES = new ConcurrentHashMap<>();

    private static ApplicationId convertId(String appId) {
        String[] s = appId.split("_");
        return ApplicationId.newInstance(Long.parseLong(s[1]), Integer.parseInt(s[2]));
    }

    public static YarnClient createYarnClient(String resourceManagerAddress) {
        try {
            Configuration configuration = new YarnConfiguration();
            configuration.set("yarn.resourcemanager.address", resourceManagerAddress);
            YarnClient yarnClient = YarnClient.createYarnClient();
            yarnClient.init(configuration);
            yarnClient.start();
            return yarnClient;
        } catch (Exception e) {
            String msg = "get yarn client error";
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public static YarnClient getYarnClient(String rsAddress) {
        return CLIENT_CACHES.computeIfAbsent(rsAddress, i -> createYarnClient(rsAddress));
    }

    public static boolean isValid(String applicationId) {
        Pattern pattern = compile("^application_\\d{13}_\\d{4,6}$");
        Matcher m = pattern.matcher(applicationId);
        return m.find();
    }

    private static List<String> getList(String origin) {
        Pattern pattern = compile("application_\\d{13}_\\d{4,6}");
        Matcher m = pattern.matcher(origin);

        List<String> applicationIds = Lists.newArrayList();
        while (m.find()) {
            applicationIds.add(m.group());
        }
        return applicationIds;
    }

    public static void kill(String rsAddress, String applicationId) throws IOException, YarnException {
        List<String> list = getList(applicationId);
        YarnClient yarnClient = getYarnClient(rsAddress);
        for (String id : list) {
            yarnClient.killApplication(convertId(id));
        }
    }

    public static YarnApplicationState getAppStatus(String rsAddress, String applicationId) {
        ApplicationReport applicationReport = null;
        try {
            applicationReport = getYarnClient(rsAddress).getApplicationReport(convertId(applicationId));
        } catch (YarnException | IOException e) {
            e.printStackTrace();
            LOGGER.error("get status error", e);
        }
        if (applicationReport == null) {
            throw new IllegalStateException("can't get application report");
        }
        return applicationReport.getYarnApplicationState();
    }

    public static List<String> getApplicationIds(String rsAddress, String name) throws IOException, YarnException {
        return Lists.newArrayList("application_" + System.currentTimeMillis() + "_12345");
//        List<ApplicationReport> applications = getYarnClient(rsAddress).getApplications(EnumSet.of(YarnApplicationState.RUNNING));
//        return applications.stream().filter(i -> name.equals(i.getName()))
//                .map(ApplicationReport::getApplicationId)
//                .map(ApplicationId::toString)
//                .collect(Collectors.toList());
    }
}
