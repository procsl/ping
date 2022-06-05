package cn.procsl.ping.admin.listener;

import cn.procsl.ping.boot.infra.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.infra.domain.rbac.Role;
import cn.procsl.ping.boot.infra.domain.user.RegisterService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Indexed
@Component
@RequiredArgsConstructor
public class AdminStartedListener implements ApplicationListener<ApplicationReadyEvent> {

    final String account = "admin";
    final String password = "123456";
    final RegisterService registerService;

    final ConfigOptionService configOptionService;

    final JpaRepository<Role, Long> roleJpaRepository;

    @Override
    @Transactional
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        log.info("应用启动完成!!!");
        String init = configOptionService.get("初始化系统数据");
        if (!ObjectUtils.isEmpty(init)) {
            log.info("系统默认数据已于[{}]初始化完成", init);
            return;
        }
        log.info("创建默认角色:{}", "系统管理员角色");
        Role role = new Role("系统管理员角色");
        roleJpaRepository.save(role);

        log.info("注册默认账户:{}", account);
        this.registerService.register(account, password);

        this.configOptionService.put("初始化系统数据", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
    }

}
