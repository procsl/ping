package cn.procsl.ping.boot.system.listener;

import cn.procsl.ping.boot.common.service.PasswordEncoderService;
import cn.procsl.ping.boot.system.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.system.domain.rbac.HttpPermission;
import cn.procsl.ping.boot.system.domain.rbac.Permission;
import cn.procsl.ping.boot.system.domain.rbac.Role;
import cn.procsl.ping.boot.system.domain.rbac.Subject;
import cn.procsl.ping.boot.system.domain.user.User;
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
import java.util.List;

@Slf4j
@Indexed
@Component
@RequiredArgsConstructor
public class SystemStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    final String account = "admin";
    final String password = "123456";

    final String roleName = "系统管理员角色";

    final JpaRepository<Role, Long> roleJpaRepository;

    final JpaRepository<Permission, Long> permissionJpaRepository;

    final JpaRepository<User, Long> userLongJpaRepository;

    final JpaRepository<Subject, Long> subjectLongJpaRepository;

    final PasswordEncoderService passwordEncoderService;
    final ConfigOptionService configOptionService;

    @Override
    @Transactional
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        log.info("应用启动完成!!!");
        String init = configOptionService.get("初始化系统数据");
        if (!ObjectUtils.isEmpty(init)) {
            log.info("系统默认数据已于[{}]初始化完成", init);
            return;
        }
        log.info("创建默认角色:{}", roleName);

        // 创建权限
        List<Permission> permissions = List.of(
                HttpPermission.create("GET", "/**"),
                HttpPermission.create("POST", "/**"),
                HttpPermission.create("DELETE", "/**"),
                HttpPermission.create("PATCH", "/**"),
                HttpPermission.create("PUT", "/**")
        );
        this.permissionJpaRepository.saveAll(permissions);

        // 创建角色
        Role role = new Role(roleName);
        // 添加权限
        role.addPermissions(permissions);
        this.roleJpaRepository.save(role);

        // 创建账户
        String password = passwordEncoderService.encode(this.password);
        User user = User.creator(account, account, password);
        this.userLongJpaRepository.save(user);
        log.info("注册默认账户:{}", account);

        // 创建subject, 为用户授权
        Subject subject = new Subject(user.getId());
        subject.grant(role);
        this.subjectLongJpaRepository.save(subject);

        this.configOptionService.put("初始化系统数据",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
    }

}
