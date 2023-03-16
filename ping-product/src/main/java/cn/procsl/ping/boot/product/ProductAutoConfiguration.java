package cn.procsl.ping.boot.product;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@RequiredArgsConstructor
@EnableTransactionManagement
@ConditionalOnMissingBean({ProductAutoConfiguration.class})
@EntityScan(basePackages = "cn.procsl.ping.boot.product.domain")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.product.domain", bootstrapMode = BootstrapMode.LAZY)
public class ProductAutoConfiguration {

}
