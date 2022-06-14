package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.admin.utils.QueryBuilder;
import cn.procsl.ping.admin.web.FormatPage;
import cn.procsl.ping.admin.web.MarkPageable;
import cn.procsl.ping.boot.infra.domain.user.Gender;
import cn.procsl.ping.boot.infra.domain.user.User;
import cn.procsl.ping.boot.infra.domain.user.UserRegisterService;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @Transactional
    @PostMapping("/v1/users")
    @Operation(summary = "创建用户", description = "创建用户时, 用户账户默认为用户昵称, 用户昵称可通过后期修改完成, 用户账户不可修改")
    public Long register(@Validated @RequestBody RegisterDTO registerDTO) {
        return this.userUserRegisterService.register(registerDTO.getAccount(), registerDTO.getPassword());
    }

    @Transactional
    @PatchMapping("/v1/users/{id}")
    @Operation(summary = "更新用户信息")
    public void update(@PathVariable Long id, @Validated @RequestBody UserPropDTO userPropDTO) {
        User user = this.jpaRepository.getById(id);
        user.updateSelf(userPropDTO.getName(), userPropDTO.getGender(), userPropDTO.getRemark());
    }

    @MarkPageable
    @GetMapping("/v1/users")
    @Transactional(readOnly = true)
    @Operation(summary = "获取用户列表")
    public FormatPage<UserDetailsVO> findUsers(Pageable pageable, @RequestParam(required = false) String name,
                                               @RequestParam(required = false) String account,
                                               @RequestParam(required = false) Gender gender) {

        Expression<AccountVO> accountProjection = Projections.bean(AccountVO.class, qaccount.name, qaccount.state).as("account");
        QBean<UserDetailsVO> projections = Projections.bean(UserDetailsVO.class, quser.id, quser.name, quser.gender, quser.remark, accountProjection);

        JPQLQuery<UserDetailsVO> query = queryFactory.select(projections).from(quser)
                .innerJoin(qaccount).on(quser.account.id.eq(qaccount.id));

        val builder = QueryBuilder
                .builder(query)
                .and(name, () -> quser.name.like(String.format("%%%s%%", name)))
                .and(account, () -> qaccount.name.like(String.format("%%%s%%", account)))
                .and(gender != null, () -> quser.gender.eq(gender));

        return FormatPage.page(builder, pageable);
    }


}
