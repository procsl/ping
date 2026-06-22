package cn.procsl.ping.boot.system.api.ui;

import cn.procsl.ping.boot.common.BusinessException;
import cn.procsl.ping.boot.system.domain.ui.Component;
import cn.procsl.ping.boot.system.domain.ui.ResourceReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "UI", description = "系统UI接口")
public class UIController {


    @Operation(summary = "用户菜单列表")
    @ResourceReference(name = "用户菜单列表")
    @PutMapping(path = "/v1/system/menus")
    @ResponseStatus(HttpStatus.OK)
    public List<Component> menus() throws BusinessException {
        return null;
    }

}
