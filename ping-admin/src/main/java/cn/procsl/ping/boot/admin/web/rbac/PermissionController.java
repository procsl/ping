package cn.procsl.ping.boot.admin.web.rbac;

import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.domain.rbac.QPermission;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.utils.QueryBuilder;
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


@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "permissions", description = "权限管理模块")
public class PermissionController {

    final JpaRepository<Permission, Long> permissionJpaRepository;

    final JPQLQueryFactory queryFactory;

    final QPermission qpermission = QPermission.permission;

    final MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);

    @Transactional
    @PostMapping("/v1/permissions")
    @Operation(summary = "创建权限")
    public PermissionVO create(@Validated @RequestBody PermissionCreateDTO permission) throws BusinessException {
        Permission entity = permission.convert();
        permissionJpaRepository.save(entity);
        return this.mapStructMapper.mapper(entity);
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
    public void update(@PathVariable Long id, @Validated @RequestBody PermissionUpdateDTO permission)
            throws BusinessException {
        permissionJpaRepository.getReferenceById(id)
                               .update(permission.getOperate(),
                                       permission.getResource());
    }

    @MarkPageable
    @Transactional(readOnly = true)
    @Operation(summary = "查询角色权限")
    @GetMapping("/v1/permissions")
    public FormatPage<PermissionVO> findPermissions(Pageable pageable,
                                                    @RequestParam(required = false) String resource,
                                                    @RequestParam(required = false) PermissionType type) {
        val from = type != null ? type.query : this.qpermission;
        QBean<PermissionVO> details = Projections.bean(PermissionVO.class, from);

        JPQLQuery<PermissionVO> query = this.queryFactory.select(details).from(from);

        val builder = QueryBuilder
                .builder(query)
                .and(resource, () -> qpermission.operate.like(String.format("%%%s%%", resource)));

        return FormatPage.page(builder, pageable);
    }

}
