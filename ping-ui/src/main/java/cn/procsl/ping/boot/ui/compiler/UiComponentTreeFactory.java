package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import cn.procsl.ping.boot.ui.exception.ComposeException;
import cn.procsl.ping.boot.ui.repository.ComposeRepository;
import cn.procsl.ping.boot.ui.repository.OpenApiRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 编译组装入口（design phase-2 / D8）：扫描 compose.json + openapi-*.json
 * → 校验输入完整性 → 编译 app 树。
 *
 * <p>无 Spring 依赖，直接 new 使用、用完即释放（DDD 无状态原则）；
 * 自动配置层只做装配，编译流程内聚于 compiler 包。
 *
 * <p>边界契约：
 * <ul>
 *   <li>无 compose.json → 返回空 app 树（children 空，200 空树语义）</li>
 *   <li>存在 compose.json 而无任何 openapi 文件 → {@link ComposeException}
 *       （启动失败语义，design D8，不在请求期返回 500）</li>
 * </ul>
 */
public final class UiComponentTreeFactory {

    /**
     * classpath 默认输入编译（运行时装配入口）。
     */
    public static Component create() {
        ObjectMapper mapper = new ObjectMapper();
        return create(new ComposeRepository(mapper), new OpenApiRepository(mapper));
    }

    /**
     * 注入输入源编译（包级可见，供单测以空/断链输入覆盖边界与失败语义）。
     */
    static Component create(ComposeRepository composeRepository, OpenApiRepository openApiRepository) {
        List<JsonNode> compose = composeRepository.loadAll();
        List<JsonNode> documents = openApiRepository.loadDocuments();

        if (!compose.isEmpty() && documents.isEmpty()) {
            throw new ComposeException(
                "compose.json exists but no META-INF/ping/openapi-*.json found in classpath"
                    + " (run the openapi export build first, or remove the compose declaration)");
        }

        return new ComposeCompiler(List.of(new OperationTransformer()))
            .compile(compose, new CompileContext(documents));
    }

    private UiComponentTreeFactory() {
    }
}
