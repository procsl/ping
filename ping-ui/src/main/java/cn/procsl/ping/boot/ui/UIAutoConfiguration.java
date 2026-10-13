package cn.procsl.ping.boot.ui;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
// 控制器与 @ResourceReference 标注需要被注册到宿主上下文（ping-system 等模块不扫描 ping-ui 包）
@ComponentScan(basePackages = "cn.procsl.ping.boot.ui.component.api")
public class UIAutoConfiguration {

}
