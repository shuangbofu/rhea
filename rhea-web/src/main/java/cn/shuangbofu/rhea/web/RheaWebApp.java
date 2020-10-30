package cn.shuangbofu.rhea.web;

import cn.shuangbofu.rhea.web.persist.dao.BaseDao;
import cn.shuangbofu.rhea.web.persist.entity.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author shuangbofu
 */
@SpringBootApplication
@ComponentScan(basePackages = {"cn.shuangbofu.rhea.web.**"})
@ImportResource(locations = {"classpath:spring.xml"})
@EnableScheduling
public class RheaWebApp {

    public static void main(String[] args) {
        try {
            SpringApplication.run(RheaWebApp.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Value("${runtime.env}")
    public void setEnv(String value) {
        Model.ENV = value;
        BaseDao.ENV = value;
    }

    @Controller
    public static class Index {

        @RequestMapping("/")
        public String index() {
            return "index.html";
        }
    }
}
