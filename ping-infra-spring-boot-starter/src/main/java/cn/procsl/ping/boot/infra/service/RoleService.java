package cn.procsl.ping.boot.infra.service;

import cn.procsl.ping.boot.domain.valid.UniqueField;
import cn.procsl.ping.boot.infra.domain.rbac.Role;
import cn.procsl.ping.exception.BusinessException;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;

public interface RoleService {
    Long create(@NotNull @UniqueField(entity = Role.class, fieldName = "name", message = "角色已存在") String name, @NotNull @Size(max = 100) Collection<@NotBlank @Size(max = 200) String> permissions) throws BusinessException;

    void delete(@NotNull Long roleId) throws BusinessException;

    void changePermissions(@NotNull Long id, @NotNull @Size(max = 100) Collection<@NotBlank @Size(max = 200) String> permissions) throws BusinessException;

    void changeRoleName(@NotNull Long id, @NotBlank @Size(max = 20) String name) throws ConstraintViolationException;
}
