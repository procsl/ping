package cn.procsl.ping.boot.rest.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * @author procsl
 * @date 2020/03/13
 */
@Configuration
@AutoConfigureBefore({HttpMessageConvertersAutoConfiguration.class})
public class RestHttpMessageConverterAutoConfiguration {


}
