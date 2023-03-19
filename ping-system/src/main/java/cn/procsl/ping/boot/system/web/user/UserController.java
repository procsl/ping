package cn.procsl.ping.boot.system.web.user;

import cn.procsl.ping.boot.common.service.PasswordEncoderService;
import cn.procsl.ping.boot.system.domain.rbac.Role;
import cn.procsl.ping.boot.system.domain.rbac.Subject;
import cn.procsl.ping.boot.system.domain.user.RoleSettingService;
import cn.procsl.ping.boot.system.domain.user.User;
import cn.procsl.ping.boot.web.Changed;
import cn.procsl.ping.boot.web.Created;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "users", description = "用户管理模块接口")
public class UserController {

    final JpaRepository<User, Long> jpaRepository;

    final JpaSpecificationExecutor<Role> jpaSpecificationExecutor;

    final JpaRepository<Subject, Long> subjectLongJpaRepository;

    final PasswordEncoderService passwordEncoderService;

    final RoleSettingService roleSettingService;

    @Created(path = "/v1/system/users", summary = "创建用户", description = "用户昵称可通过后期修改完成, 用户账户不可修改")
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

    @Changed(path = "/v1/system/users/{id}", summary = "更新用户信息")
    public void update(@PathVariable Long id, @Validated @RequestBody UserPropDTO userPropDTO) {
        User user = this.jpaRepository.getReferenceById(id);
        user.updateSelf(userPropDTO.getName(), userPropDTO.getGender(), userPropDTO.getRemark());
    }

//    @MarkPageable
//    @Query(path = "/v1/system/users", summary = "获取用户列表")
//    public FormatPage<UserDetailsVO> findUsers(Pageable pageable,
//                                               @RequestParam(required = false) String name,
//                                               @RequestParam(required = false, name = "account.name") String account,
//                                               @RequestParam(required = false, name = "account.state") AccountState state,
//                                               @RequestParam(required = false) Gender gender) {
//
//        Expression<AccountVO> accountProjection = Projections.bean(AccountVO.class, qaccount.name, qaccount.state)
//                                                             .as("account");
//        QBean<UserDetailsVO> projections = Projections.bean(UserDetailsVO.class, quser.id, quser.name, quser.gender,
//                quser.remark, accountProjection);
//
//        JPQLQuery<UserDetailsVO> query = queryFactory.select(projections).from(quser).innerJoin(qaccount)
//                                                     .on(quser.account.id.eq(qaccount.id));
//
//        val builder = QueryBuilder.builder(query)
//                                  .and(name, () -> quser.name.like(String.format("%%%s%%", name)))
//                                  .and(account, () -> qaccount.name.like(String.format("%%%s%%", account)))
//                                  .and(state != null, () -> qaccount.state.eq(state))
//                                  .and(gender != null, () -> quser.gender.eq(gender));
//
//        return FormatPage.page(builder, pageable);
//    }


}
