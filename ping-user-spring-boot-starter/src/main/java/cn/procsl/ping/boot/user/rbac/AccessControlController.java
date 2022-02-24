package cn.procsl.ping.boot.user.rbac;

import cn.procsl.ping.business.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping
@Tag(name = "访问控制服务", description = "访问控制,权限相关接口")
public class AccessControlController {

    final AccessControlService accessControlService;

    @Transactional
    @PostMapping("roles")
    @Operation(operationId = "createRole", description = "创建角色")
    @ResponseStatus(code = HttpStatus.CREATED, reason = "创建成功")
    public Long createRole(@RequestBody @Validated CreateRoleDTO createRoleDTO) throws BusinessException {
        return this.accessControlService.createRole(createRoleDTO.getName(), createRoleDTO.getPermissions());
    }

}
