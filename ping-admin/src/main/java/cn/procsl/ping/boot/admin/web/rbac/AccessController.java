package cn.procsl.ping.boot.admin.web.rbac;

import cn.procsl.ping.boot.admin.domain.rbac.*;
import cn.procsl.ping.boot.common.error.BusinessException;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "users")
public class AccessController {

    final JPQLQueryFactory queryFactory;

    final JpaSpecificationExecutor<Subject> subjectJpaSpecificationExecutor;

    final JpaRepository<Role, Long> roleLongJpaRepository;

    final QSubject sub = QSubject.subject1;

    final QRole qrole = QRole.role;

    @Transactional
    @PostMapping("/v1/users/{id}/roles")
    @Operation(summary = "授予角色", operationId = "grantRoles")
    public void grant(@PathVariable("id") Long id,
                      @RequestBody @NotNull @Validated @Schema(description = "角色ID") Collection<Long> roleIds) {
        Optional<Subject> optional = this.subjectJpaSpecificationExecutor.findOne(
                new SubjectRoleSpecification(id, null));
        Subject subject = optional.orElseThrow(() -> new BusinessException("目标用户不存在"));
        List<Role> roles = this.roleLongJpaRepository.findAllById(roleIds);
        subject.grant(roles);
    }

    @Transactional(readOnly = true)
    @GetMapping("/v1/users/{id}/roles")
    @Operation(summary = "获取已授权列表")
    public Collection<RoleVO> findSubjects(@PathVariable("id") Long id) {
        val select = Projections.bean(RoleVO.class, qrole.name, qrole.id);
        return this.queryFactory.select(select).from(sub).innerJoin(sub.roles, qrole).where(sub.subject.eq(id)).fetch();
    }

}
