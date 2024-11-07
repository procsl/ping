package cn.procsl.ping.boot.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//@EnableScheduling
@SpringBootApplication
@EnableBatchProcessing(modular = true)
public class TestBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestBatchApplication.class, args);
    }


}
