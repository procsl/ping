package cn.procsl.ping.boot.system.api.user;

import cn.procsl.ping.boot.common.service.PasswordEncoderService;
import cn.procsl.ping.boot.system.domain.rbac.Role;
import cn.procsl.ping.boot.system.domain.rbac.Subject;
import cn.procsl.ping.boot.system.domain.user.RoleSettingService;
import cn.procsl.ping.boot.system.domain.user.User;
import cn.procsl.ping.boot.web.annotation.SecurityId;
import cn.procsl.ping.boot.web.hateoas.ResourceLink;
import cn.procsl.ping.boot.web.hateoas.SingletonResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "用户管理模块接口")
public class UserController {

    final JpaRepository<User, Long> jpaRepository;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    final JpaSpecificationExecutor<Role> jpaSpecificationExecutor;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    final JpaRepository<Subject, Long> subjectLongJpaRepository;

    final PasswordEncoderService passwordEncoderService;

    final RoleSettingService roleSettingService;

    final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Operation(summary = "创建用户")
    @PostMapping(path = "/v1/system/users")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional(rollbackFor = Exception.class)
    public SingletonResource<UserDetailVO> create(@Validated @RequestBody RegisterDTO register) {
        String password = register.getPassword();
        User user = User.creator(register.getNickName(), register.getAccount(),
                passwordEncoderService.encode(password));
        this.jpaRepository.save(user);

        Collection<String> roleNames = this.roleSettingService.getDefaultRoles();

        if (!ObjectUtils.isEmpty(roleNames)) {
            Subject subject = new Subject(user.getId());

            List<Role> roles = this.jpaSpecificationExecutor.findAll(
                    (root, query, criteriaBuilder) -> root.get("name").in(roleNames));
            subject.grant(roles);
            subjectLongJpaRepository.save(subject);
        }

        UserDetailVO detail = this.userMapper.mapper(user);
        Link links = Link.of("/v1/system/users/{id}", "self")
                .withMedia(APPLICATION_JSON_VALUE);
        return SingletonResource.createResource(detail, ResourceLink.of(links));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping(path = "/v1/system/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(rollbackFor = Exception.class)
    public void update(@PathVariable @SecurityId(scope = "user") Long id, @Validated @RequestBody UserPropDTO prop) {
        User user = this.jpaRepository.getReferenceById(id);
        user.updateSelf(prop.getName(), prop.getGender(), prop.getRemark());
    }


}
