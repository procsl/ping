package cn.procsl.ping.app;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static cn.procsl.ping.app.DistributeApplication.name;
import static cn.procsl.ping.app.TestDistributeApplication.apache;
import static cn.procsl.ping.app.TestDistributeApplication.desc;


@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "接口文档", version = "1.0", license = @License(url = apache, name = name), description = desc))
public class TestDistributeApplication {

    final static String name = "Apache License 2.0";

    final static String desc = "Ping接口参考文档, 可用于指导生成测试用例, HTTP Client SDK";

    final static String apache = "https://www.apache.org/licenses/LICENSE-2.0";

    public static void main(String[] args) {

        SpringApplication.run(TestDistributeApplication.class, args);
    }

}