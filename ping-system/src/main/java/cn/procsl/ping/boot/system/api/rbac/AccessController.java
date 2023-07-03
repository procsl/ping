package cn.procsl.ping.boot.system.api.rbac;

import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.system.domain.rbac.Role;
import cn.procsl.ping.boot.system.domain.rbac.Subject;
import cn.procsl.ping.boot.system.domain.rbac.SubjectRoleSpec;
import cn.procsl.ping.boot.web.annotation.SecurityId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "User")
public class AccessController {

    final JpaSpecificationExecutor<Subject> subjectJpaSpecificationExecutor;

    final JpaRepository<Role, Long> roleLongJpaRepository;

    @Operation(summary = "授予角色权限")
    @PostMapping(path = "/v1/system/users/{id}/roles")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(rollbackFor = Exception.class)
//    @Schema(description = "角色ID", example = "[]")
    public void grant(@PathVariable("id")
                      @SecurityId Long id,
                      @RequestBody
                      @NotNull
                      @Validated Collection<Long> roleIds) {
        Optional<Subject> optional = this.subjectJpaSpecificationExecutor.findOne(
                new SubjectRoleSpec(id, null));
        Subject subject = optional.orElseThrow(() -> new BusinessException("目标用户不存在"));
        List<Role> roles = this.roleLongJpaRepository.findAllById(roleIds);
        subject.grant(roles);
    }

//    @Query(path = "/v1/system/users/{id}/roles", summary = "获取已授权列表")
//    public Collection<RoleVO> findSubjects(@PathVariable("id") Long id) {
//        val select = Projections.bean(RoleVO.class, qrole.name, qrole.id);
//        return this.queryFactory.select(select).from(sub).innerJoin(sub.roles, qrole).where(sub.subject.eq(id)).fetch();
//    }

}
