package cn.procsl.ping.app.admin;

//import io.swagger.v3.oas.annotations.OpenAPIDefinition;
//import io.swagger.v3.oas.annotations.info.Info;
//import io.swagger.v3.oas.annotations.info.License;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * starter
 *
 * @author procsl
 * @date 2020/04/26
 */
@OpenAPIDefinition(
    info = @Info(
        title = "接口文档",
        version = "1.0",
        license = @License(url = "https://www.apache.org/licenses/LICENSE-2.0", name = "Apache License 2.0"),
        description = "Ping接口参考文档, 可用于指导生成测试用例, HTTP Client SDK"
    )
)
@SpringBootApplication(scanBasePackages = {"cn.procsl.ping"})
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

}
