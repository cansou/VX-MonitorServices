package chs.wechat.spy;

import chs.wechat.spy.context.SpringStartContext;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@MapperScan(basePackages = "chs.wechat.spy.mybatis.mapper")
@SpringBootApplication
@ComponentScan(basePackages = {"chs.wechat.spy.controller"})
public class VxMonitorServicesApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(VxMonitorServicesApplication.class);
        app.addListeners(new SpringStartContext());
        app.run(args);
    }

}