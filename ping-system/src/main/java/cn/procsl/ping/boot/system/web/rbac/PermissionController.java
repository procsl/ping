package cn.procsl.ping.boot.system.web.rbac;

import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.error.ExceptionResolver;
import cn.procsl.ping.boot.common.event.Publisher;
import cn.procsl.ping.boot.common.web.Changed;
import cn.procsl.ping.boot.common.web.Created;
import cn.procsl.ping.boot.common.web.Deleted;
import cn.procsl.ping.boot.system.domain.rbac.Permission;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static cn.procsl.ping.boot.system.constant.EventPublisherConstant.*;


@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "permissions", description = "权限管理模块")
public class PermissionController {

    final JpaRepository<Permission, Long> permissionJpaRepository;

    final MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);

    @Created(path = "/v1/system/permissions", summary = "创建权限")
    @Publisher(name = PERMISSION_CREATE_EVENT, parameter = "#id")
    public PermissionVO create(@Validated @RequestBody PermissionCreateDTO permission) throws BusinessException {
        Permission entity = permission.convert();
        permissionJpaRepository.save(entity);
        return this.mapStructMapper.mapper(entity);
    }

    @Deleted(path = "/v1/system/permissions/{id}", summary = "删除权限")
    @ExceptionResolver(message = "该角色已被关联, 角色删除失败", code = "409001", matcher = DataIntegrityViolationException.class)
    @Publisher(name = PERMISSION_DELETE_EVENT, parameter = "#id")
    public void delete(@PathVariable Long id) throws BusinessException {
        this.permissionJpaRepository.deleteById(id);
    }

    @Changed(path = "/v1/system/permissions/{id}", summary = "更新权限")
    @Publisher(name = PERMISSION_UPDATE_EVENT, parameter = "#id")
    public void update(@PathVariable Long id, @Validated @RequestBody PermissionUpdateDTO permission)
            throws BusinessException {
        permissionJpaRepository.getReferenceById(id)
                .update(permission.getOperate(),
                        permission.getResource());
    }

//    @MarkPageable
//    @Query(path = "/v1/system/permissions", summary = "查询角色权限")
//    public FormatPage<PermissionVO> findPermissions(Pageable pageable,
//                                                    @RequestParam(required = false) String resource,
//                                                    @RequestParam(required = false) PermissionType type) {
//        val query = this.queryFactory.select(qpermission).from(qpermission);
//
//        val builder = QueryBuilder
//                .builder(query)
//                .and(resource, () -> qpermission.operate.like(String.format("%%%s%%", resource)))
//                .and(type != null, () -> {
//                    assert type != null;
//                    return qpermission.instanceOf(type.getType());
//                });
//
//        return FormatPage.page(builder, pageable).transform(this.mapStructMapper::mapper);
//    }

}
