package cn.procsl.ping.boot.admin.web.rbac;


import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.domain.rbac.QRole;
import cn.procsl.ping.boot.admin.domain.rbac.Role;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.error.ExceptionResolver;
import cn.procsl.ping.boot.common.utils.QueryBuilder;
import cn.procsl.ping.boot.common.validator.UniqueValidator;
import cn.procsl.ping.boot.common.web.FormatPage;
import cn.procsl.ping.boot.common.web.MarkPageable;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
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

    final MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);


    @Transactional
    @PostMapping("/v1/roles")
    @Operation(summary = "创建角色")
    public RoleVO createRole(@Validated @RequestBody RoleGrantDTO details) throws BusinessException {
        uniqueValidator.valid(Role.class, null, "name", details.getName(), "角色已存在");
        List<Permission> permissions = this.permissionJpaRepository.findAllById(details.getPermissions());
        Role entity = new Role(details.getName(), permissions);
        roleRepository.save(entity);
        return mapStructMapper.mapper(entity);
    }

    @Transactional
    @Operation(summary = "删除角色")
    @DeleteMapping("/v1/roles/{id}")
    @ExceptionResolver(message = "该角色正在使用中,无法删除")
    public void deleteRole(@PathVariable Long id) throws BusinessException {
        this.roleRepository.deleteById(id);
    }

    @Transactional
    @PatchMapping("/v1/roles/{id}")
    @Operation(summary = "修改指定角色信息")
    public void changeRole(@PathVariable("id") Long id,
                           @Validated({Default.class}) @RequestBody @NotNull RoleGrantDTO details)
            throws BusinessException {
        uniqueValidator.valid(Role.class, id, "name", details.getName(), "角色已存在");
        Role role = this.roleRepository.getReferenceById(id);
        List<Permission> permissions = this.permissionJpaRepository.findAllById(details.getPermissions());
        role.change(details.getName(), permissions);
    }

    @Transactional
    @GetMapping("/v1/roles/{id}")
    @Operation(summary = "获取指定角色信息")
    public RolePermissionVO getRoleById(@PathVariable("id") Long id) throws BusinessException {
        Role role = this.roleRepository.getReferenceById(id);
        return this.mapStructMapper.mapperDetails(role);
    }

    @MarkPageable
    @Transactional(readOnly = true)
    @Operation(summary = "查询角色权限")
    @GetMapping("/v1/roles")
    public FormatPage<RoleVO> findRoles(Pageable pageable, @RequestParam(required = false) String name) {
        QBean<RoleVO> details = Projections.bean(RoleVO.class, qrole.id, qrole.name);

        JPQLQuery<RoleVO> query = this.queryFactory.select(details).from(qrole);

        val builder = QueryBuilder.builder(query).and(name, () -> qrole.name.like(String.format("%%%s%%", name)));

        return FormatPage.page(builder, pageable);
    }

}
