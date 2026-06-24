package cn.procsl.ping.boot.system.api.ui;

import cn.procsl.ping.boot.common.BusinessException;
import cn.procsl.ping.boot.system.domain.ui.ResourceReference;
import cn.procsl.ping.boot.system.domain.ui.UiComponent;
import cn.procsl.ping.boot.system.domain.ui.UiSchemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "UI", description = "系统UI接口")
public class UIController {

    UiSchemaService ui = new UiSchemaService();

    @Operation(summary = "用户菜单列表")
    @ResourceReference(name = "用户菜单列表")
    @GetMapping(path = "/v1/system/menus", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<UiComponent> menus() throws BusinessException {
        return ui.loadAll();
    }

}
