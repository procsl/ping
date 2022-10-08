package cn.procsl.ping.boot.admin.web.user;

import cn.procsl.ping.boot.admin.domain.rbac.DataPermissionFilter;
import cn.procsl.ping.boot.admin.domain.user.*;
import cn.procsl.ping.boot.common.utils.QueryBuilder;
import cn.procsl.ping.boot.common.web.FormatPage;
import cn.procsl.ping.boot.common.web.MarkPageable;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "users", description = "用户管理模块接口")
public class UserController {

    final static QUser quser = QUser.user;

    final static QAccount qaccount = QAccount.account;

    final JpaRepository<User, Long> jpaRepository;

    final JPQLQueryFactory queryFactory;

    final UserRegisterService userUserRegisterService;

    final DepartService departService;

    @Transactional
    @PostMapping("/v1/users")
    @Operation(summary = "创建用户", operationId = "createUser", description = "创建用户时, 用户账户默认为用户昵称, " +
            "用户昵称可通过后期修改完成, 用户账户不可修改")
    public Long register(@Validated @RequestBody RegisterDTO registerDTO) {
        return this.userUserRegisterService.register(registerDTO.getAccount(), registerDTO.getPassword());
    }

    @Transactional
    @PatchMapping("/v1/users/{id}")
    @Operation(summary = "更新用户信息", operationId = "updateUser")
    public void update(@PathVariable Long id, @Validated @RequestBody UserPropDTO userPropDTO) {
        User user = this.jpaRepository.getReferenceById(id);
        user.updateSelf(userPropDTO.getName(),
                userPropDTO.getGender(),
                userPropDTO.getCardNumber(),
                userPropDTO.getNation(),
                userPropDTO.getBirthday(),
                userPropDTO.getGraduateSchool(),
                userPropDTO.getTelephone(),
                userPropDTO.getEmail(),
                userPropDTO.getRemark()
        );
    }

    @MarkPageable
    @GetMapping("/v1/users")
    @Transactional(readOnly = true)
    @Operation(summary = "获取用户列表")
    @DataPermissionFilter(filter = "#root[currentUserDataPermission]?.apply('读取-所有用户列表权限')",
            executor = "#root[overrideArgument]?.apply(#arguments, #root[currentAccount].get()?.id, 1)")
    public FormatPage<UserDetailsVO> findUsers(Pageable pageable,
                                               @Parameter(hidden = true) @RequestParam(required = false) Long id,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false, name = "account.name") String account,
                                               @RequestParam(required = false, name = "account.state") AccountState state,
                                               @RequestParam(required = false) Gender gender) {

        Expression<AccountVO> accountProjection = Projections.bean(AccountVO.class, qaccount.name, qaccount.state)
                                                             .as("account");
        QBean<UserDetailsVO> projections = Projections.bean(UserDetailsVO.class,
                quser.id,
                quser.name,
                quser.gender,
                quser.birthday,
                quser.cardNumber,
                quser.email,
                quser.graduateSchool,
                quser.nation,
                quser.telephone,
                quser.remark,
                accountProjection);

        JPQLQuery<UserDetailsVO> query = queryFactory.select(projections).from(quser).innerJoin(qaccount)
                                                     .on(quser.account.id.eq(qaccount.id));

        val builder = QueryBuilder.builder(query)
                                  .and(id != null, () -> quser.id.eq(id))
                                  .and(name, () -> quser.name.like(String.format("%%%s%%", name)))
                                  .and(account, () -> qaccount.name.like(String.format("%%%s%%", account)))
                                  .and(state != null, () -> qaccount.state.eq(state))
                                  .and(gender != null, () -> quser.gender.eq(gender));


        FormatPage<UserDetailsVO> tmp = FormatPage.page(builder, pageable);
        Collection<Long> ids = tmp.convert(UserVO::getId);
        Map<Long, DepartmentDTO> departNames = this.departService.getDepartNames(ids);
        for (UserDetailsVO vo : tmp.getContent()) {
            vo.setDepartment(departNames.get(vo.getId()));
        }

        return tmp;
    }


}
