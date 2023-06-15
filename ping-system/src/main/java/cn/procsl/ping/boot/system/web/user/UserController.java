package cn.procsl.ping.boot.system.web.user;

import cn.procsl.ping.boot.common.service.PasswordEncoderService;
import cn.procsl.ping.boot.system.domain.rbac.Role;
import cn.procsl.ping.boot.system.domain.rbac.Subject;
import cn.procsl.ping.boot.system.domain.user.RoleSettingService;
import cn.procsl.ping.boot.system.domain.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "用户管理模块接口")
public class UserController {

    final JpaRepository<User, Long> jpaRepository;

    final JpaSpecificationExecutor<Role> jpaSpecificationExecutor;

    final JpaRepository<Subject, Long> subjectLongJpaRepository;

    final PasswordEncoderService passwordEncoderService;

    final RoleSettingService roleSettingService;

    @Operation(summary = "创建用户", description = "用户昵称可通过后期修改完成, 用户账户不可修改")
    @PostMapping(path = "/v1/system/users")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional(rollbackFor = Exception.class)
    public void register(@Validated @RequestBody RegisterDTO registerDTO) {

        String password = registerDTO.getPassword();
        User user = User.creator(registerDTO.getAccount(), registerDTO.getAccount(),
                passwordEncoderService.encode(password));
        this.jpaRepository.save(user);

        Collection<String> roleNames = this.roleSettingService.getDefaultRoles();

        if (ObjectUtils.isEmpty(roleNames)) {
            return;
        }

        Subject subject = new Subject(user.getId());

        List<Role> roles = this.jpaSpecificationExecutor.findAll(
                (root, query, criteriaBuilder) -> root.get("name").in(roleNames));
        subject.grant(roles);
        subjectLongJpaRepository.save(subject);

    }

    @Operation(summary = "更新用户信息")
    @PutMapping(path = "/v1/system/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(rollbackFor = Exception.class)
    public void update(@PathVariable Long id, @Validated @RequestBody UserPropDTO userPropDTO) {
        User user = this.jpaRepository.getReferenceById(id);
        user.updateSelf(userPropDTO.getName(), userPropDTO.getGender(), userPropDTO.getRemark());
    }


}
