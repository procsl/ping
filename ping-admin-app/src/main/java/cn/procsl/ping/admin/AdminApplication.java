package cn.procsl.ping.admin;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

import static cn.procsl.ping.admin.AdminApplication.apache;
import static cn.procsl.ping.admin.AdminApplication.desc;

/**
 * starter
 *
 * @author procsl
 * @date 2020/04/26
 */
@EnableHypermediaSupport(type = {EnableHypermediaSupport.HypermediaType.HAL})
@SpringBootApplication(scanBasePackages = {"cn.procsl.ping.admin"}, exclude = ErrorMvcAutoConfiguration.class)
@OpenAPIDefinition(info = @Info(title = "接口文档", version = "1.0", license = @License(url = apache, name = "Apache License 2.0"), description = desc))
public class AdminApplication {

    public static final String desc = "Ping接口参考文档, 可用于指导生成测试用例, HTTP Client SDK";

    public static final String apache = "https://www.apache.org/licenses/LICENSE-2.0";

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

}
