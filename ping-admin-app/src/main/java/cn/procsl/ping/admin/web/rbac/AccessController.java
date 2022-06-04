package cn.procsl.ping.admin.web.rbac;

import cn.procsl.ping.boot.infra.domain.rbac.AccessControlService;
import cn.procsl.ping.boot.infra.domain.rbac.QRole;
import cn.procsl.ping.boot.infra.domain.rbac.QSubject;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "users")
public class AccessController {

    final AccessControlService accessControlService;

    final JPQLQueryFactory queryFactory;

    final QSubject sub = QSubject.subject1;

    final QRole qrole = QRole.role;

    @Transactional
    @PostMapping("/v1/users/{id}/roles")
    @Operation(summary = "授予角色")
    public void grant(@PathVariable("id") Long id, @RequestBody @NotNull @Validated @Schema(description = "角色名称") Collection<String> roles) {
        this.accessControlService.grant(id, roles);
    }

    @Transactional(readOnly = true)
    @GetMapping("/v1/users/{id}/roles")
    @Operation(summary = "获取已授权列表")
    public Collection<RoleVO> findSubjects(@PathVariable("id") Long id) {
        val select = Projections.bean(RoleVO.class, qrole.name, qrole.id);
        return this.queryFactory.select(select).from(sub).innerJoin(sub.roles, qrole).where(sub.subject.eq(id)).fetch();
    }

}
