package cn.procsl.ping.boot.oa;

import cn.procsl.ping.boot.oa.adapter.ApiHubCalendarServiceAdapter;
import cn.procsl.ping.boot.oa.domain.clock.HolidayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@AutoConfiguration
@EntityScan(basePackages = "cn.procsl.ping.boot.oa.domain")
@EnableJpaRepositories(basePackages = "cn.procsl.ping.boot.oa.domain", bootstrapMode = BootstrapMode.LAZY)
@ComponentScan(basePackages = {
        "cn.procsl.ping.boot.oa.web",
})
@EnableCaching
public class OaAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public HolidayService holidayService(@Autowired(required = false) RestTemplate restTemplate) {
        if (restTemplate == null) {
            restTemplate = new RestTemplateBuilder()
                    .setReadTimeout(Duration.ofSeconds(3))
                    .setBufferRequestBody(true)
                    .defaultHeader("User-Agent", "procsl-ping-oa")
                    .setConnectTimeout(Duration.ofSeconds(6)).build();
        }
        return new ApiHubCalendarServiceAdapter(restTemplate);
    }

}
