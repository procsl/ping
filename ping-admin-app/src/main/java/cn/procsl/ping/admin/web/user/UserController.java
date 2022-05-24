package cn.procsl.ping.admin.web.user;

import cn.procsl.ping.boot.infra.domain.account.QAccount;
import cn.procsl.ping.boot.infra.domain.user.QUser;
import cn.procsl.ping.boot.infra.domain.user.User;
import cn.procsl.ping.boot.infra.service.UserService;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.web.PageableDefault;
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

    @PostMapping("users")
    @Operation(description = "创建用户")
    public Long register(@Validated @RequestBody UserDTO userDTO) {
        return this.userService.register(userDTO.getAccount(), userDTO.getPassword());
    }

    @GetMapping("users/{id}")
    @Operation(description = "获取用户信息")
    @Transactional(readOnly = true)
    public User getById(@PathVariable Long id) {
        return jpaRepository.getById(id);
    }

    @GetMapping("users")
    @Transactional(readOnly = true)
    @Operation(description = "获取用户列表")
    public Page<UserVO> findUsers(@PageableDefault Pageable pageable) {
        QUser quser = QUser.user;
        QAccount qaccount = QAccount.account;
        QBean<UserVO> projections = Projections.bean(UserVO.class, quser.id, quser.name, quser.gender, quser.remark, qaccount.name.as("account"));

        JPQLQuery<UserVO> query = queryFactory.select(projections)
                .from(quser)
                .innerJoin(qaccount).on(quser.accountId.eq(qaccount.id))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .orderBy(quser.id.desc());

        QueryResults<UserVO> results = query.fetchResults();

        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }


}
