package cn.procsl.ping.admin.web.rbac;


import cn.procsl.ping.admin.annotation.MarkPageable;
import cn.procsl.ping.admin.utils.QueryBuilder;
import cn.procsl.ping.admin.web.FormatPage;
import cn.procsl.ping.boot.domain.valid.UniqueValidator;
import cn.procsl.ping.boot.infra.domain.rbac.QRole;
import cn.procsl.ping.boot.infra.domain.rbac.Role;
import cn.procsl.ping.exception.BusinessException;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "roles", description = "角色权限管理模块")
public class RoleController {

    final JpaRepository<Role, Long> roleRepository;

    final UniqueValidator uniqueValidator;

    final JPQLQueryFactory queryFactory;

    final QRole qrole = QRole.role;


    @Transactional
    @PostMapping("/v1/roles")
    @Operation(summary = "创建角色")
    public Long create(@Validated @RequestBody RoleDetailsDTO role) throws BusinessException {
        uniqueValidator.valid(Role.class, null, "name", role.getName(), "角色已存在");
        Role entity = new Role(role.getName(), role.getPermissions());
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
    public void change(@PathVariable("id") Long id, @RequestBody @Validated @NotNull RoleNameDTO name) throws BusinessException {
        uniqueValidator.valid(Role.class, id, "name", name.getName(), "角色已存在");
        Role role = this.roleRepository.getById(id);
        role.change(name.getName(), null);
    }

    @Transactional
    @GetMapping("/v1/roles/{id}")
    @Operation(summary = "获取指定角色信息")
    public RoleDetailsDTO findById(@PathVariable("id") Long id) throws BusinessException {
        Role role = this.roleRepository.getById(id);
        return new RoleDetailsDTO(role);
    }

    @Transactional
    @Operation(summary = "修改角色权限")
    @PatchMapping("/v1/roles/{id}/permissions")
    @Parameter(name = "id", description = "角色ID")
    public void change(@PathVariable("id") Long id,
                       @RequestBody @Validated @Schema(description = "权限列表") @NotNull @Size(max = 100)
                       Collection<@NotBlank @Size(max = 200) String> permissions) throws BusinessException {
        Role role = this.roleRepository.getById(id);
        role.changePermissions(permissions);
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
