package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.admin.MarkPageable;
import cn.procsl.ping.admin.web.FormatPage;
import cn.procsl.ping.boot.infra.domain.account.QAccount;
import cn.procsl.ping.boot.infra.domain.user.QUser;
import cn.procsl.ping.boot.infra.domain.user.User;
import cn.procsl.ping.boot.infra.service.UserService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "用户管理模块")
public class UserController {

    final UserService userService;

    final JpaRepository<User, Long> jpaRepository;

    final JPQLQueryFactory queryFactory;

    final static QUser quser = QUser.user;

    @PostMapping("users")
    @Operation(description = "创建用户")
    public Long register(@Validated @RequestBody RegisterDTO registerDTO) {
        return this.userService.register(registerDTO.getAccount(), registerDTO.getPassword());
    }

    @GetMapping("users/{id}")
    @Operation(description = "获取用户信息")
    @Transactional(readOnly = true)
    public UserDetailsVO getById(@PathVariable Long id) {
        QBean<UserDetailsVO> projections = Projections.bean(UserDetailsVO.class, quser.id, quser.name, quser.gender, quser.remark, QAccount.account.name.as("account"));
        return queryFactory.select(projections).from(quser).innerJoin(QAccount.account).on(quser.accountId.eq(QAccount.account.id)).where(quser.id.eq(id)).fetchFirst();
    }

    @MarkPageable
    @GetMapping("users")
    @Transactional(readOnly = true)
    @Operation(description = "获取用户列表")
    public FormatPage<UserVO> findUsers(Pageable pageable, @ParameterObject UserVO user) {

        QBean<UserVO> projections = Projections.bean(UserVO.class, quser.id, quser.name, quser.gender, quser.remark);

        BooleanBuilder builder = new BooleanBuilder();

        JPQLQuery<UserVO> query = queryFactory.select(projections).from(quser).limit(pageable.getPageSize()).offset(pageable.getOffset()).orderBy(quser.id.desc());

        QueryResults<UserVO> results = query.fetchResults();

        return new FormatPage<>(results.getResults(), pageable, results.getTotal());
    }


}
