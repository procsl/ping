package cn.procsl.ping.admin.web.rbac;


import cn.procsl.ping.admin.utils.QueryBuilder;
import cn.procsl.ping.admin.web.FormatPage;
import cn.procsl.ping.admin.web.MarkPageable;
import cn.procsl.ping.boot.base.domain.rbac.Permission;
import cn.procsl.ping.boot.base.domain.rbac.QRole;
import cn.procsl.ping.boot.base.domain.rbac.Role;
import cn.procsl.ping.boot.domain.valid.UniqueValidator;
import cn.procsl.ping.common.exception.BusinessException;
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

import javax.validation.constraints.NotNull;
import java.util.List;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "roles", description = "角色管理模块")
public class RoleController {

    final JpaRepository<Role, Long> roleRepository;

    final JpaRepository<Permission, Long> permissionJpaRepository;

    final UniqueValidator uniqueValidator;

    final JPQLQueryFactory queryFactory;

    final QRole qrole = QRole.role;


    @Transactional
    @PostMapping("/v1/roles")
    @Operation(summary = "创建角色")
    public Long create(@Validated @RequestBody RoleDetailsDTO roleDetails) throws BusinessException {
        uniqueValidator.valid(Role.class, null, "name", roleDetails.getName(), "角色已存在");
        List<Permission> permissions = this.permissionJpaRepository.findAllById(roleDetails.getPermissions());
        Role entity = new Role(roleDetails.getName(), permissions);
        return roleRepository.save(entity).getId();
    }

    @Transactional
    @Operation(summary = "删除角色")
    @DeleteMapping("/v1/roles/{id}")
    public void delete(@PathVariable Long id) throws BusinessException {
        this.roleRepository.deleteById(id);
    }

    @Transactional
    @PatchMapping("/v1/roles/{id}")
    @Operation(summary = "修改指定角色信息")
    public void change(@PathVariable("id") Long id, @RequestBody @NotNull RoleDetailsDTO details) throws BusinessException {
        uniqueValidator.valid(Role.class, id, "name", details.getName(), "角色已存在");
        Role role = this.roleRepository.getReferenceById(id);
        List<Permission> permissions = this.permissionJpaRepository.findAllById(details.getPermissions());
        role.change(details.getName(), permissions);
    }

    @Transactional
    @GetMapping("/v1/roles/{id}")
    @Operation(summary = "获取指定角色信息")
    public RoleDetailsDTO getById(@PathVariable("id") Long id) throws BusinessException {
        Role role = this.roleRepository.getReferenceById(id);
        return new RoleDetailsDTO(role);
    }

    @MarkPageable
    @Transactional(readOnly = true)
    @Operation(summary = "查询角色权限")
    @GetMapping("/v1/roles")
    public FormatPage<RoleVO> findRoles(Pageable pageable, @RequestParam(required = false) String name) {
        QBean<RoleVO> details = Projections.bean(RoleVO.class, qrole.id, qrole.name);

        JPQLQuery<RoleVO> query = this.queryFactory
                .select(details)
                .from(qrole);

        val builder = QueryBuilder
                .builder(query)
                .and(name, () -> qrole.name.like(String.format("%%%s%%", name)));

        return FormatPage.page(builder, pageable);
    }

}
