package cn.procsl.ping.boot.rbac.config;

import cn.procsl.ping.boot.rbac.domain.facade.Initialization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author procsl
 * @date 2020/04/10
 */
@Slf4j
public class DefaultInitialization implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired(required = false)
    Initialization initialization;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (initialization == null) {
            log.debug("InitializationEvent未找到, 忽略初始化数据处理");
            return;
        }

        boolean bool = event.getApplicationContext().getParent() != null;
        if (bool) {
            return;
        }

        log.info("开始初始化系统默认数据");
        initialization.action();
    }
}
