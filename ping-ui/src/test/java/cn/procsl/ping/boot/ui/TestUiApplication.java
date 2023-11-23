package cn.procsl.ping.boot.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "cn.procsl.ping.boot.ui")
public class TestUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestUiApplication.class, args);
    }

}
