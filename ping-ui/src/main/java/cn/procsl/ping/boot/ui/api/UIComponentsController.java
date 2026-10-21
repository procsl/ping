package cn.procsl.ping.boot.ui.api;

import cn.procsl.ping.boot.ui.compiler.UiComponentTreeFactory;
import cn.procsl.ping.boot.ui.domain.Component;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 编译产物端点（design phase-2 / spec ui-components-endpoint）：
 * {@code GET /v1/ui/components} 返回编译后的 app 树。
 *
 * <p>无状态（DDD 原则）：不持有产物快照，请求时直接经 {@link UiComponentTreeFactory}
 * 现场编译；构造时调用一次 factory 仅作启动验证——输入缺失/断链等结构性错误
 * 在启动阶段即失败（design D8，不在请求期返回 500）。
 *
 * <p>鉴权跟随全局登录拦截器：不加 {@code @PermitAll}，未登录 401。
 */
@Slf4j
@Tag(name = "UI", description = "UI 组件树接口：功能树编译产物查询")
@RestController
public class UIComponentsController {

    public UIComponentsController() {
        // 启动阶段验证一次：输入完整可编译，否则启动失败；产物不持有
        UiComponentTreeFactory.create();
        log.debug("UI 组件树编译验证通过（端点每次请求现场编译）");
    }

    @Operation(
        summary = "获取编译后的 UI 功能树",
        description = "返回编译后的 app 树（app > page > function），"
            + "function.properties 为 openapi operation 自包含内联结果（method/path/"
            + "parameters/requestBody/responses-2xx + route，无 $ref 残留）；"
            + "compose.json 缺省时返回仅含 app 根的空树。")
    @GetMapping("/v1/ui/components")
    public Component components() {
        return UiComponentTreeFactory.create();
    }
}
