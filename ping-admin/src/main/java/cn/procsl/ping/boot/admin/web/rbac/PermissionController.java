package cn.procsl.ping.boot.admin.web.rbac;

import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.domain.rbac.QPermission;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.error.ExceptionResolver;
import cn.procsl.ping.boot.common.event.Publisher;
import cn.procsl.ping.boot.common.utils.QueryBuilder;
import cn.procsl.ping.boot.common.web.*;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.*;


@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "permissions", description = "权限管理模块")
public class PermissionController {

    final JpaRepository<Permission, Long> permissionJpaRepository;

    final JPQLQueryFactory queryFactory;

    final QPermission qpermission = QPermission.permission;

    final MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);

    @Created(path = "/v1/permissions", summary = "创建权限")
    @Publisher(name = PERMISSION_CREATE_EVENT, parameter = "#id")
    public PermissionVO create(@Validated @RequestBody PermissionCreateDTO permission) throws BusinessException {
        Permission entity = permission.convert();
        permissionJpaRepository.save(entity);
        return this.mapStructMapper.mapper(entity);
    }

    @Deleted(path = "/v1/permissions/{id}", summary = "删除权限")
    @ExceptionResolver(message = "该角色已被关联, 角色删除失败", code = "409001", matcher = DataIntegrityViolationException.class)
    @Publisher(name = PERMISSION_DELETE_EVENT, parameter = "#id")
    public void delete(@PathVariable Long id) throws BusinessException {
        this.permissionJpaRepository.deleteById(id);
    }

    @Changed(path = "/v1/permissions/{id}", summary = "更新权限")
    @Publisher(name = PERMISSION_UPDATE_EVENT, parameter = "#id")
    public void update(@PathVariable Long id, @Validated @RequestBody PermissionUpdateDTO permission)
            throws BusinessException {
        permissionJpaRepository.getReferenceById(id)
                               .update(permission.getOperate(),
                                       permission.getResource());
    }

    @MarkPageable
    @QueryDetails(path = "/v1/permissions", summary = "查询角色权限")
    public FormatPage<PermissionVO> findPermissions(Pageable pageable,
                                                    @RequestParam(required = false) String resource,
                                                    @RequestParam(required = false) PermissionType type) {
        val query = this.queryFactory.select(qpermission).from(qpermission);

        val builder = QueryBuilder
                .builder(query)
                .and(resource, () -> qpermission.operate.like(String.format("%%%s%%", resource)))
                .and(type != null, () -> {
                    assert type != null;
                    return qpermission.instanceOf(type.getType());
                });

        return FormatPage.page(builder, pageable).transform(this.mapStructMapper::mapper);
    }

}
