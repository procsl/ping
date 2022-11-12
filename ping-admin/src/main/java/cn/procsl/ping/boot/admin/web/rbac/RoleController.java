package cn.procsl.ping.boot.admin.web.rbac;


import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.domain.rbac.QRole;
import cn.procsl.ping.boot.admin.domain.rbac.Role;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.error.ExceptionResolver;
import cn.procsl.ping.boot.common.event.Publisher;
import cn.procsl.ping.boot.common.utils.QueryBuilder;
import cn.procsl.ping.boot.common.web.*;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.List;

import static cn.procsl.ping.boot.admin.constant.EventPublisherConstant.*;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "roles", description = "角色管理模块")
public class RoleController {


    final JpaRepository<Role, Long> roleRepository;

    final JpaRepository<Permission, Long> permissionJpaRepository;

    final JPQLQueryFactory queryFactory;

    final QRole qrole = QRole.role;

    final MapStructMapper mapStructMapper = Mappers.getMapper(MapStructMapper.class);


    @Created(path = "/v1/admin/roles", summary = "创建角色")
    @Publisher(name = ROLE_CREATE_EVENT, parameter = "#details.name")
    @ExceptionResolver(matcher = ConstraintViolationException.class, message = "角色已存在")
    public RoleVO createRole(@Validated @RequestBody RoleGrantDTO details) throws BusinessException {
        List<Permission> permissions = this.permissionJpaRepository.findAllById(details.getPermissions());
        Role entity = new Role(details.getName(), permissions);
        roleRepository.save(entity);
        return mapStructMapper.mapper(entity);
    }

    @Deleted(path = "/v1/admin/roles/{id}", summary = "删除角色")
    @ExceptionResolver(message = "该角色正在使用中,无法删除", code = "409001")
    @Publisher(name = ROLE_DELETE_EVENT, parameter = "#id")
    public void deleteRole(@PathVariable Long id) throws BusinessException {
        this.roleRepository.deleteById(id);
    }

    @Changed(path = "/v1/admin/roles/{id}", summary = "修改指定角色信息")
    @Publisher(name = ROLE_CHANGED_EVENT, parameter = "#id")
    public void changeRole(@PathVariable("id") Long id,
                           @Validated({Default.class}) @RequestBody @NotNull RoleGrantDTO details)
            throws BusinessException {
        Role role = this.roleRepository.getReferenceById(id);
        List<Permission> permissions = this.permissionJpaRepository.findAllById(details.getPermissions());
        role.change(details.getName(), permissions);
    }

    @Query(path = "/v1/admin/roles/{id}", summary = "获取指定角色信息")
    public RolePermissionVO getRoleById(@PathVariable("id") Long id) throws BusinessException {
        Role role = this.roleRepository.getReferenceById(id);
        return this.mapStructMapper.mapperDetails(role);
    }

    @MarkPageable
    @Query(path = "/v1/admin/roles", summary = "查询角色权限")
    public FormatPage<RoleVO> findRoles(Pageable pageable, @RequestParam(required = false) String name) {
        QBean<RoleVO> details = Projections.bean(RoleVO.class, qrole.id, qrole.name);

        JPQLQuery<RoleVO> query = this.queryFactory.select(details).from(qrole);

        val builder = QueryBuilder.builder(query).and(name, () -> qrole.name.like(String.format("%%%s%%", name)));

        return FormatPage.page(builder, pageable);
    }

}
