package cn.procsl.ping.boot.editor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@ConditionalOnMissingBean({EditorAutoConfiguration.class})
//@EntityScan(basePackages = "cn.procsl.ping.boot.editro.domain")
@ComponentScan(basePackages = "cn.procsl.ping.boot.editor")
public class EditorAutoConfiguration {


}
