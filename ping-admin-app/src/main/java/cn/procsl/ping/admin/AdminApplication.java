package cn.procsl.ping.admin;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;


/**
 * starter
 *
 * @author procsl
 * @date 2020/04/26
 */
@SpringBootApplication(scanBasePackages = {"cn.procsl.ping.admin"},
        exclude = {
//                ErrorMvcAutoConfiguration.class,
        })
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }

}
