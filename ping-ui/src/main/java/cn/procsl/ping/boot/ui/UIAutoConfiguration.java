package cn.procsl.ping.boot.ui;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;

@EntityScan(basePackages = "cn.procsl.ping.boot.ui.domain")
@EnableJpaRepositories(basePackages = {"cn.procsl.ping.boot.ui.domain"}, bootstrapMode = BootstrapMode.LAZY)
public class UIAutoConfiguration {


}
