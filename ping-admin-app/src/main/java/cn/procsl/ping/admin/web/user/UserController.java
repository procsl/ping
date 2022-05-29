package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.admin.annotation.MarkPageable;
import cn.procsl.ping.admin.utils.QueryBuilder;
import cn.procsl.ping.admin.web.FormatPage;
import cn.procsl.ping.boot.infra.domain.account.QAccount;
import cn.procsl.ping.boot.infra.domain.user.Gender;
import cn.procsl.ping.boot.infra.domain.user.QUser;
import cn.procsl.ping.boot.infra.domain.user.RegisterService;
import cn.procsl.ping.boot.infra.domain.user.User;
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

    final static QAccount qaccount = QAccount.account;

    final JpaRepository<User, Long> jpaRepository;

    final JPQLQueryFactory queryFactory;

    final static QUser quser = QUser.user;
    final RegisterService userRegisterService;

    @Transactional
    @PostMapping("/v1/users")
    @Operation(summary = "创建用户", description = "创建用户时, 用户账户默认为用户昵称, 用户昵称可通过后期修改完成, 用户账户不可修改")
    public Long register(@Validated @RequestBody RegisterDTO registerDTO) {
        return this.userRegisterService.register(registerDTO.getAccount(), registerDTO.getPassword());
    }

    @GetMapping("/v1/users/{id}")
    @Operation(summary = "获取用户信息")
    @Transactional(readOnly = true)
    public UserDetailsVO getById(@PathVariable Long id) {
        QBean<UserDetailsVO> projections = Projections.bean(UserDetailsVO.class,
                quser.id, quser.name, quser.gender, quser.remark,
                QAccount.account.name.as("account"));
        return queryFactory
                .select(projections)
                .from(quser)
                .innerJoin(qaccount).on(quser.accountId.eq(qaccount.id))
                .where(quser.id.eq(id)).fetchFirst();
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
    public FormatPage<UserVO> findUsers(Pageable pageable, @RequestParam(required = false) String name, @RequestParam(required = false) Gender gender) {

        QBean<UserVO> projections = Projections.bean(UserVO.class, quser.id, quser.name, quser.gender, quser.remark);
        JPQLQuery<UserVO> query = queryFactory.select(projections).from(quser);

        val builder = QueryBuilder
                .builder(query)
                .and(name, () -> quser.name.like(String.format("%%%s%%", name)))
                .and(gender != null, () -> quser.gender.eq(gender));

        return FormatPage.page(builder, pageable);
    }


}
