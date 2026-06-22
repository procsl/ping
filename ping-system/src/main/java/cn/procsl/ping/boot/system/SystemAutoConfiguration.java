package cn.procsl.ping.boot.system;

import cn.procsl.ping.boot.jpa.support.EnableDomainRepositories;
import cn.procsl.ping.boot.system.api.user.AuthenticateInterceptor;
import cn.procsl.ping.boot.system.constant.SystemConfigureProperties;
import cn.procsl.ping.boot.system.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static cn.procsl.ping.boot.web.RestWebAutoConfiguration.PUBLIC_STATIC_RESOURCES;

@Slf4j
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@EnableTransactionManagement
@EntityScan(basePackages = "cn.procsl.ping.boot.system.domain")
@EnableJpaRepositories(basePackages = {"cn.procsl.ping.boot.system.domain"}, bootstrapMode = BootstrapMode.LAZY)
@ComponentScan(basePackages = {"cn.procsl.ping.boot.system.api", "cn.procsl.ping.boot.system.query", "cn.procsl.ping.boot.system.service", "cn.procsl.ping.boot.system.adapter"}, basePackageClasses = {ConfigService.class})
@EnableConfigurationProperties(SystemConfigureProperties.class)
@RequiredArgsConstructor
public class SystemAutoConfiguration implements WebMvcConfigurer {

    final SystemConfigureProperties systemConfigureProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticateInterceptor(systemConfigureProperties.getAuthenticatesPrefix()))
            .excludePathPatterns(PUBLIC_STATIC_RESOURCES).addPathPatterns("/**");
    }


    @Configuration(proxyBeanMethods = false)
    @EnableDomainRepositories(basePackages = "cn.procsl.ping.boot.system.query")
    public static class DomainRepositoryConfigurer {

    }

    @Bean
    @ConditionalOnProperty(name = "openapi.export", havingValue = "true")
    public ApplicationListener<ApplicationReadyEvent> openApiJsonExporter(ApplicationContext context) {
        log.warn("Open API JSON exporter, 导出后进程将关闭");
        return (event) -> {
            String outputDir = context.getEnvironment().getProperty("openapi.output.dir");
            Integer port = context.getEnvironment().getProperty("local.server.port", Integer.class);

            try {
                log.info("[OpenAPI] 正在通过 RestClient 请求下载...");
                // 2. 使用 Spring Boot 的 RestClient 优雅地发起请求
                String json = RestClient.create("http://127.0.0.1:" + port)
                    .get()
                    .uri("/v3/api-docs")
                    .retrieve()
                    .body(String.class);
                if (json == null) {
                    throw new RuntimeException("/v3/api-docs 接口响应为空");
                }

                // 3. 使用 Spring 的 FileCopyUtils 一句话搞定写文件
                File targetFile = new File(outputDir, "openapi.json");
                if(targetFile.getParentFile().mkdirs()){
                    log.warn("临时文件创建失败");
                }
                FileCopyUtils.copy(json.getBytes(StandardCharsets.UTF_8), targetFile);
                log.info("[OpenAPI] 成功导出至: {}", targetFile.getAbsolutePath());
            } catch (Exception e) {
                log.error("[OpenAPI] 下载失败", e);
                SpringApplication.exit(context, () -> 1);
            }
            SpringApplication.exit(context, () -> 0);
        };
    }
}
