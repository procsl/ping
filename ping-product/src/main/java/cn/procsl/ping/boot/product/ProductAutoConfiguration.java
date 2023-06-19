package cn.procsl.ping.boot.product;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

@Slf4j
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@ConditionalOnMissingBean({ProductAutoConfiguration.class})
@EntityScan(basePackages = "cn.procsl.ping.boot.product.domain")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.product.domain", bootstrapMode = BootstrapMode.LAZY)
@ComponentScan(basePackages = "cn.procsl.ping.boot.product")
public class ProductAutoConfiguration {


}
