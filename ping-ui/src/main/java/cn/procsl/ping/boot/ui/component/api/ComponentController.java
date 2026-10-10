package cn.procsl.ping.boot.ui.component.api;

import cn.procsl.ping.boot.ui.component.UIComponentRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 组件描述接口：从内存注册表下发抽象组件类型定义与装配后的组件树。
 *
 * <p>注册表在上下文刷新期一次性装配，本接口只读快照，不访问数据库、不读取描述文件。
 */
@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "Component", description = "抽象组件描述接口")
public class ComponentController {

    private static final String DESCRIPTION =
        "返回内存注册表中的组件类型定义（types）与装配后的组件树（pages）；"
            + "注册表为空时返回两个空数组。描述文件损坏、接口引用未命中等一律降级，不影响本接口。";

    final UIComponentRegistry registry;

    @Operation(summary = "获取抽象组件类型定义与组件树", description = DESCRIPTION)
    @GetMapping(value = "/v1/ui/components", produces = MediaType.APPLICATION_JSON_VALUE)
    public UIComponentRegistry.ComponentResponse queryComponents() {
        return registry.snapshot();
    }

}
