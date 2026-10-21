package cn.procsl.ping.boot.ui.compiler;

import cn.procsl.ping.boot.ui.domain.Component;
import cn.procsl.ping.boot.ui.exception.ComposeException;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 主编排器（design D6/D9）：
 * <ul>
 *   <li>入口：compose 源 {@code JsonNode} 列表 → {@link Component} 树转换（未知类型/属性忽略，2.5）</li>
 *   <li>链式：{@code nodes = t.process(nodes, context)} 执行各 {@link Transformer}
 *       （纯变换 List→List，每步可过滤/增删/改字段）</li>
 *   <li>固定：{@code assemble} 合并为 {@code type:"app"} 根（app 根是不变量，不做成扩展点）</li>
 * </ul>
 *
 * <p>无状态：直接 new 创建、用完即释放（AGENTS DDD 无状态原则）。
 */
@Slf4j
public class ComposeCompiler {

    /**
     * 产物 app 根标识（phase-1 产物示例）。
     */
    private static final String APP_ID = "ping.app";

    /**
     * compose 源合法节点类型：page / function（app 根由编译生成，源中出现即未知类型）。
     */
    private static final Set<String> SOURCE_TYPES = Set.of("page", "function");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final List<Transformer> transformers;

    public ComposeCompiler(List<Transformer> transformers) {
        this.transformers = List.copyOf(transformers);
    }

    /**
     * 完整编译：转换 → 链式变换 → 组装 app 根。
     *
     * @param sourceNodes compose 源节点数组（各模块 compose.json 合并后的列表）
     * @param context     openapi 编译上下文（完整文档 + operationId 索引）
     * @return {@code type:"app"} 根节点
     * @throws cn.procsl.ping.boot.ui.exception.ComposeException 编译期任一结构性错误（启动失败）
     */
    public Component compile(List<JsonNode> sourceNodes, CompileContext context) {
        List<Component> nodes = toComponents(sourceNodes);
        validateIds(nodes);                      // 结构校验：page.id 全局唯一、function.id 页内唯一（D8）
        for (Transformer t : transformers) {
            nodes = t.process(nodes, context);   // 链式：每步可过滤/增删/改字段
        }
        nodes = injectRoutes(nodes, null);       // 路由生成：function.properties.route = page.id + "/" + function.id（D5）
        return assemble(nodes);                  // 固定方法：合并为 type:"app" 根
    }

    // --- 路由生成（design D5：全局标识 / 前端路由 = page.id + "/" + function.id） ---

    private static List<Component> injectRoutes(List<Component> nodes, String pageId) {
        List<Component> result = new ArrayList<>(nodes.size());
        for (Component node : nodes) {
            if (node.isType("function")) {
                result.add(withRoute(node, pageId));
            } else if (node.isType("page")) {
                result.add(new Component(node.getType(), node.getId(), node.getProperties(),
                    injectRoutes(node.getChildren(), node.getId())));
            } else {
                result.add(node);
            }
        }
        return List.copyOf(result);
    }

    private static Component withRoute(Component function, String pageId) {
        if (function.getId() == null) {
            return function; // 无 id 无法生成路由（唯一性校验同样跳过 null id）
        }
        String route = pageId != null ? pageId + "/" + function.getId() : function.getId();

        JsonNode properties = function.getProperties();
        ObjectNode updated = properties != null && properties.isObject()
            ? ((ObjectNode) properties).deepCopy()
            : MAPPER.createObjectNode();
        updated.put("route", route);
        return new Component(function.getType(), function.getId(), updated, function.getChildren());
    }

    // --- id 唯一性校验（design D8：任一重复 → 启动失败，报错含声明上下文） ---

    private static void validateIds(List<Component> nodes) {
        collectPageIds(nodes, new HashSet<>());
    }

    private static void collectPageIds(List<Component> nodes, Set<String> pageIds) {
        for (Component node : nodes) {
            if (node.isType("page")) {
                String pageId = node.getId();
                if (pageId != null && !pageIds.add(pageId)) {
                    throw new ComposeException("Duplicate page id '" + pageId
                        + "' (page.id must be globally unique, declared in multiple compose sources)");
                }
                validateFunctionIds(pageId, node.getChildren());
                collectPageIds(node.getChildren(), pageIds); // 递归覆盖嵌套 page
            }
        }
    }

    private static void validateFunctionIds(String pageId, List<Component> children) {
        Set<String> functionIds = new HashSet<>();
        for (Component child : children) {
            if (child.isType("function") && child.getId() != null && !functionIds.add(child.getId())) {
                throw new ComposeException("Duplicate function id '" + child.getId()
                    + "' in page '" + pageId
                    + "' (function.id must be unique within its page)");
            }
        }
    }

    /**
     * 组装 app 根（稳定逻辑，app 根结构是不变量，不做成扩展点）。
     */
    private Component assemble(List<Component> nodes) {
        return new Component("app", APP_ID, null, nodes);
    }

    // --- JsonNode → Component 转换（入口统一完成，插件只见 Component） ---

    private static List<Component> toComponents(List<JsonNode> source) {
        List<Component> result = new ArrayList<>(source.size());
        for (JsonNode node : source) {
            Component component = toComponent(node);
            if (component != null) {
                result.add(component);
            }
        }
        return List.copyOf(result);
    }

    /**
     * 单节点转换；未知类型（page/function 之外）与非对象节点忽略（design D8：忽略 + debug 日志）。
     */
    private static Component toComponent(JsonNode node) {
        if (node == null || !node.isObject()) {
            log.debug("忽略 compose 源中的非对象节点: {}", node);
            return null;
        }

        JsonNode typeNode = node.get("type");
        if (typeNode == null || !typeNode.isString()) {
            log.debug("忽略缺少 type 的 compose 节点: {}", node);
            return null;
        }
        if (!SOURCE_TYPES.contains(typeNode.asString())) {
            log.debug("忽略未知类型节点 type='{}'（合法类型: page/function）: {}", typeNode.asString(), node);
            return null;
        }

        JsonNode idNode = node.get("id");
        String id = idNode != null && idNode.isString() ? idNode.asString() : null;

        JsonNode propertiesNode = node.get("properties");
        JsonNode properties = propertiesNode != null && propertiesNode.isObject()
            ? propertiesNode : null;

        JsonNode childrenNode = node.get("children");
        List<Component> children = childrenNode != null && childrenNode.isArray()
            ? toComponents(toList(childrenNode))
            : List.of();

        return new Component(typeNode.asString(), id, properties, children);
    }

    private static List<JsonNode> toList(JsonNode arrayNode) {
        List<JsonNode> items = new ArrayList<>(arrayNode.size());
        for (JsonNode item : arrayNode) {
            items.add(item);
        }
        return items;
    }
}
