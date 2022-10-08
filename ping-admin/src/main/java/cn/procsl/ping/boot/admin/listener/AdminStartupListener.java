package cn.procsl.ping.boot.admin.listener;

import cn.procsl.ping.boot.admin.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.admin.domain.rbac.*;
import cn.procsl.ping.boot.admin.domain.user.UserRegisterService;
import cn.procsl.ping.boot.admin.service.HttpPermissionRegistry;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Indexed
@Component
@RequiredArgsConstructor
public class AdminStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    final String account = "admin";
    final String password = "123456";

    final String roleName = "系统管理员角色";
    final UserRegisterService userRegisterService;

    final ConfigOptionService configOptionService;

    final JpaRepository<Role, Long> roleJpaRepository;

    final AccessControlService accessControlService;

    final JpaRepository<Permission, Long> permissionJpaRepository;

    final HttpPermissionRegistry registry;

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

        ArrayList<Permission> scanner = registry.httpPermissionScanner();
        this.permissionJpaRepository.saveAll(scanner);
        List<Permission> permissions = List.of(
                HttpPermission.create("GET", "/**"),
                HttpPermission.create("POST", "/**"),
                HttpPermission.create("DELETE", "/**"),
                HttpPermission.create("PATCH", "/**"),
                HttpPermission.create("PUT", "/**"),
                new DataPermission("读取", "所有用户列表权限"),
                new DataPermission("重置", "所有用户密码权限"),
                new DataPermission("读取", "所有用户工资信息权限"),
                new DataPermission("读取", "所有用户销售信息权限")
        );
        Role role = new Role(roleName);
        role.addPermissions(scanner);
        this.permissionJpaRepository.saveAll(permissions);
        this.roleJpaRepository.flush();
        this.roleJpaRepository.save(role);
        this.roleJpaRepository.flush();

        log.info("注册默认账户:{}", account);
        Long id = this.userRegisterService.register(account, password);
        this.accessControlService.grantByName(id, List.of(roleName));
        this.configOptionService.put("初始化系统数据",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
    }

}
