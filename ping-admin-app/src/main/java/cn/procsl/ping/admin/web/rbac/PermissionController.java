package cn.procsl.ping.admin.web.rbac;

import cn.procsl.ping.admin.utils.QueryBuilder;
import cn.procsl.ping.admin.web.FormatPage;
import cn.procsl.ping.admin.web.MarkPageable;
import cn.procsl.ping.boot.base.domain.rbac.Permission;
import cn.procsl.ping.boot.base.domain.rbac.QPermission;
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


@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "permissions", description = "权限管理模块")
public class PermissionController {

    final JpaRepository<Permission, Long> permissionJpaRepository;

    final JPQLQueryFactory queryFactory;

    final QPermission qpermission = QPermission.permission;

    @Transactional
    @PostMapping("/v1/permissions")
    @Operation(summary = "创建权限")
    public Long create(@Validated @RequestBody PermissionCreateDTO permission) throws BusinessException {
        return permissionJpaRepository.save(permission.convert()).getId();
    }

    @Transactional
    @Operation(summary = "删除权限")
    @DeleteMapping("/v1/permissions/{id}")
    public void delete(@PathVariable Long id) throws BusinessException {
        this.permissionJpaRepository.deleteById(id);
    }

    @Transactional
    @PatchMapping("/v1/permissions/{id}")
    @Operation(summary = "更新权限")
    public void update(@PathVariable Long id, @Validated @RequestBody PermissionUpdateDTO permission) throws BusinessException {
        permissionJpaRepository.getById(id).update(permission.getOption(), permission.getResource());
    }

    @MarkPageable
    @Transactional(readOnly = true)
    @Operation(summary = "查询角色权限")
    @GetMapping("/v1/permissions")
    public FormatPage<PermissionVO> findPermissions(Pageable pageable, @RequestParam(required = false) String resource, @RequestParam(required = false) PermissionType type) {
        QBean<PermissionVO> details = Projections.bean(PermissionVO.class, qpermission.id, qpermission.option, qpermission.resource);

        val from = type != null ? type.query : this.qpermission;
        JPQLQuery<PermissionVO> query = this.queryFactory.select(details).from(from);

        val builder = QueryBuilder.builder(query).and(resource, () -> qpermission.option.like(String.format("%%%s%%", resource)));

        return FormatPage.page(builder, pageable);
    }

}
