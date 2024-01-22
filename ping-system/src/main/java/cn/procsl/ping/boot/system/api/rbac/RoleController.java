package cn.procsl.ping.boot.system.api.rbac;


import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.annotation.ExceptionResolver;
import cn.procsl.ping.boot.common.annotation.Publisher;
import cn.procsl.ping.boot.system.domain.rbac.Permission;
import cn.procsl.ping.boot.system.domain.rbac.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.procsl.ping.boot.system.constant.EventPublisherConstant.*;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "Role", description = "角色管理模块")
public class RoleController {

    final JpaRepository<Role, Long> roleRepository;

    final JpaRepository<Permission, Long> permissionJpaRepository;

    final MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);


    @Operation(summary = "创建角色")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/v1/system/roles")
    @Publisher(eventName = ROLE_CREATE_EVENT, parameter = "#details.name")
    // TODO  需要定制返回值
    @ExceptionResolver(matcher = ConstraintViolationException.class, message = "角色已存在")
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public RoleVO createRole(@Validated @RequestBody RoleGrantDTO details) throws BusinessException {
        List<Permission> permissions = this.permissionJpaRepository.findAllById(details.getPermissions());
        Role entity = new Role(details.getName(), permissions);
        roleRepository.save(entity);
        return mapStructMapper.mapper(entity);
    }

    @Operation(summary = "删除角色")
    @DeleteMapping(path = "/v1/system/roles/{id}")
    @ExceptionResolver(message = "该角色正在使用中,无法删除",
//            status = HttpStatus.CONFLICT,
            code = "ROLE_CONFLICT")
    @Publisher(eventName = ROLE_DELETE_EVENT, parameter = "#id")
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(@PathVariable Long id) throws BusinessException {
        this.roleRepository.deleteById(id);
    }

    @Operation(summary = "修改指定角色信息")
    @PutMapping(path = "/v1/system/roles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Publisher(eventName = ROLE_CHANGED_EVENT, parameter = "#id")
    @Transactional(rollbackFor = Exception.class)
    public void changeRole(@PathVariable("id") Long id,
                           @Validated({Default.class}) @RequestBody @NotNull RoleGrantDTO details)
            throws BusinessException {
        Role role = this.roleRepository.getReferenceById(id);
        List<Permission> permissions = this.permissionJpaRepository.findAllById(details.getPermissions());
        role.change(details.getName(), permissions);
    }

    @Operation(summary = "获取指定角色信息")
    @GetMapping(path = "/v1/system/roles/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Transactional(readOnly = true)
    public RolePermissionVO getRoleById(@PathVariable("id") Long id) throws BusinessException {
        Role role = this.roleRepository.getReferenceById(id);
        return this.mapStructMapper.mapperDetails(role);
    }

//    @MarkPageable
//    @Query(path = "/v1/system/roles", summary = "查询角色权限")
//    public FormatPage<RoleVO> findRoles(Pageable pageable, @RequestParam(required = false) String name) {
//        QBean<RoleVO> details = Projections.bean(RoleVO.class, qrole.id, qrole.name);
//
//        JPQLQuery<RoleVO> query = this.queryFactory.select(details).from(qrole);
//
//        val builder = QueryBuilder.builder(query).and(name, () -> qrole.name.like(String.format("%%%s%%", name)));
//
//        return FormatPage.page(builder, pageable);
//    }

}
