package cn.procsl.ping.boot.ui.component;

import cn.procsl.ping.boot.ui.component.api.ResourceReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * 抽象组件内存注册表。
 *
 * <p>上下文刷新期一次性装配，之后只读，不重复读取描述文件（spec：启动后一次性装配进内存注册表）。
 *
 * <p>装配只读两个<b>固定路径</b>，不做任何 classpath 通配扫描（架构要求）：
 * <ul>
 *   <li>{@link #INDEX_PATH} —— 构建期聚合出的组件类型定义与页面装配树</li>
 *   <li>{@link #OPENAPI_PATH} —— 打包期导出的 OpenAPI 文档，用于把接口引用解析为完整描述</li>
 * </ul>
 *
 * <p>所有降级（索引缺失、条目损坏、文档缺失、引用未命中）一律告警并继续，启动永不失败。
 */
@Slf4j
public class UIComponentRegistry implements SmartInitializingSingleton {

    /** 构建期聚合索引的固定路径，运行时唯一读取点 */
    public static final String INDEX_PATH = "META-INF/ping/ui/components.json";

    /** 打包期导出的 OpenAPI 文档固定路径 */
    public static final String OPENAPI_PATH = "ping-api-doc/openapi.json";

    /** 装配描述中接口引用的标记前缀 */
    public static final String REFERENCE_PREFIX = "@ResourceReference:";

    /** 顶层类型属于其中之一时视为导航片段，合并进应用外壳树 */
    private static final Set<String> NAVIGATION_TYPES =
        Set.of("layout", "nav_group", "menu", "breadcrumb", "tabs");

    private final JsonMapper mapper = JsonMapper.builder().build();

    private final ObjectProvider<RequestMappingHandlerMapping> handlerMappings;

    private volatile List<JsonNode> types = List.of();

    private volatile List<JsonNode> pages = List.of();

    public UIComponentRegistry(ObjectProvider<RequestMappingHandlerMapping> handlerMappings) {
        this.handlerMappings = handlerMappings;
    }

    /**
     * 所有单例就绪后一次性装配 —— 此刻 RequestMappingHandlerMapping 已完成 handler 扫描，
     * 能拿到全部带 {@link ResourceReference} 的方法。
     */
    @Override
    public void afterSingletonsInstantiated() {
        assemble(readJson(INDEX_PATH), readJson(OPENAPI_PATH), annotatedEndpoints());
    }

    /** 装配快照：组件类型定义与装配后的组件树 */
    public ComponentResponse snapshot() {
        return new ComponentResponse(types, pages);
    }

    public List<JsonNode> types() {
        return types;
    }

    public List<JsonNode> pages() {
        return pages;
    }

    /**
     * 装配入口。包内可见以便测试用合成数据覆盖各类降级路径。
     *
     * @param index    构建期聚合索引，可为 null
     * @param openapi  OpenAPI 文档，可为 null
     * @param endpoints 已被 {@link ResourceReference} 标注的端点索引
     */
    void assemble(JsonNode index, JsonNode openapi, Map<String, ApiEndpoint> endpoints) {
        if (index == null || !index.isObject()) {
            log.warn("[抽象组件] 聚合索引不可用，组件注册表为空（固定路径 {}）", INDEX_PATH);
            this.types = List.of();
            this.pages = List.of();
            return;
        }

        List<ObjectNode> nextTypes = new ArrayList<>();
        for (JsonNode type : index.path("types")) {
            if (type.isObject()) {
                nextTypes.add((ObjectNode) type.deepCopy());
            } else {
                log.warn("[抽象组件] 类型定义不是 JSON 对象，已跳过");
            }
        }

        boolean resolvable = openapi != null && openapi.isObject();
        if (!resolvable) {
            log.warn("[抽象组件] OpenAPI 文档不可用（固定路径 {}），装配描述中的接口引用保持原样",
                OPENAPI_PATH);
        }

        List<ObjectNode> trees = new ArrayList<>();
        for (JsonNode page : index.path("pages")) {
            JsonNode tree = page.path("tree");
            if (!tree.isObject()) {
                log.warn("[抽象组件] 页面条目缺少 object 形态的 tree，已跳过：{}",
                    page.path("key").asString(""));
                continue;
            }
            ObjectNode copy = (ObjectNode) tree.deepCopy();
            if (resolvable) {
                resolveReferences(copy, endpoints, openapi);
            }
            trees.add(copy);
        }

        this.types = List.copyOf(nextTypes);
        this.pages = List.copyOf(mount(trees));
        log.info("[抽象组件] 组件注册表装配完成：types={}，pages={}", this.types.size(), this.pages.size());
    }

    /**
     * 装配归宿规则（spec：装配描述按顶层类型归宿）：
     * <ul>
     *   <li>顶层 application → 应用树，按 id 去重（后者覆盖）</li>
     *   <li>顶层导航容器 → 导航片段，按 order 合并进应用外壳树的对应方位</li>
     *   <li>其它类型 → 页面片段，按顶层 router 挂载到应用树中 router 相同的 menu 节点之下；
     *       <b>无匹配时不报错</b>，原样进入 pages</li>
     * </ul>
     */
    List<ObjectNode> mount(List<ObjectNode> trees) {
        List<ObjectNode> roots = new ArrayList<>();
        List<ObjectNode> navigationFragments = new ArrayList<>();
        List<ObjectNode> pageFragments = new ArrayList<>();

        for (ObjectNode tree : trees) {
            String type = tree.path("type").asString("");
            if (type.isEmpty()) {
                log.warn("[抽象组件] 装配描述缺少 type，已跳过");
            } else if ("application".equals(type)) {
                roots.add(tree);
            } else if (NAVIGATION_TYPES.contains(type)) {
                navigationFragments.add(tree);
            } else {
                pageFragments.add(tree);
            }
        }

        roots = deduplicateRoots(roots);

        List<ObjectNode> unmatched = new ArrayList<>();
        for (ObjectNode fragment : navigationFragments) {
            if (roots.isEmpty()) {
                log.warn("[抽象组件] 没有可合并的应用外壳，导航片段原样进入 pages：{}",
                    fragment.path("type").asString(""));
                unmatched.add(fragment);
                continue;
            }
            if (!mergeNavigation(roots.get(0), fragment)) {
                unmatched.add(fragment);
            }
        }

        for (ObjectNode fragment : pageFragments) {
            String router = fragment.path("router").asString("");
            ObjectNode menu = router.isEmpty() ? null : findMenuByRouter(roots, router);
            if (menu == null) {
                log.warn("[抽象组件] 页面片段没有挂载目标，原样进入 pages：type={} router={}",
                    fragment.path("type").asString(""), router);
                unmatched.add(fragment);
            } else {
                appendChild(menu, fragment);
            }
        }

        List<ObjectNode> result = new ArrayList<>(roots);
        result.addAll(unmatched);
        result.forEach(this::normalizeOrder);
        return result;
    }

    private List<ObjectNode> deduplicateRoots(List<ObjectNode> roots) {
        Map<String, ObjectNode> byId = new LinkedHashMap<>();
        int unnamed = 0;
        for (ObjectNode root : roots) {
            String id = root.path("id").asString("");
            if (id.isEmpty()) {
                id = "__unnamed_" + (unnamed++);
                log.warn("[抽象组件] 应用根缺少 id，按内部键处理：{}", id);
            }
            if (byId.put(id, root) != null) {
                log.warn("[抽象组件] 重复的应用根 id，后者覆盖：{}", id);
            }
        }
        return new ArrayList<>(byId.values());
    }

    /**
     * 把导航片段并入应用外壳树：layout 按方位并入，其余并入左侧导航。
     *
     * @return 是否成功并入
     */
    private boolean mergeNavigation(ObjectNode root, ObjectNode fragment) {
        String type = fragment.path("type").asString("");

        if ("layout".equals(type)) {
            String slot = fragment.path("layout").asString("");
            ArrayNode containers = containersOf(root);
            for (JsonNode child : containers) {
                if (!child.isObject()
                    || !"layout".equals(child.path("type").asString(""))) {
                    continue;
                }
                if (child.path("layout").asString("").equals(slot)) {
                    appendAllInto(containersOf((ObjectNode) child), fragment);
                    return true;
                }
            }
            containers.add(fragment);
            return true;
        }

        ObjectNode left = findLayoutSlot(root, "left");
        ArrayNode target = left != null ? containersOf(left) : containersOf(root);
        target.add(fragment);
        return true;
    }

    private ObjectNode findLayoutSlot(ObjectNode root, String slot) {
        for (JsonNode child : containersOf(root)) {
            if (child.isObject()
                && "layout".equals(child.path("type").asString(""))
                && slot.equals(child.path("layout").asString(""))) {
                return (ObjectNode) child;
            }
        }
        return null;
    }

    private ObjectNode findMenuByRouter(List<ObjectNode> roots, String router) {
        for (ObjectNode root : roots) {
            ObjectNode found = findMenuByRouter(root, router);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private ObjectNode findMenuByRouter(ObjectNode node, String router) {
        if ("menu".equals(node.path("type").asString(""))
            && router.equals(node.path("router").asString(""))) {
            return node;
        }
        for (JsonNode child : containersOf(node)) {
            if (child.isObject()) {
                ObjectNode found = findMenuByRouter((ObjectNode) child, router);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void appendChild(ObjectNode parent, ObjectNode child) {
        containersOf(parent).add(child);
    }

    private void appendAllInto(ArrayNode target, ObjectNode fragment) {
        target.addAll(containersOf(fragment));
    }

    private ArrayNode containersOf(ObjectNode node) {
        JsonNode existing = node.get("containers");
        if (existing instanceof ArrayNode array) {
            return array;
        }
        ArrayNode created = mapper.createArrayNode();
        node.set("containers", created);
        return created;
    }

    /**
     * 同级顺序一律由 order 决定（缺省视为 0），稳定排序保持同 order 的声明顺序。
     */
    private void normalizeOrder(ObjectNode node) {
        JsonNode children = node.get("containers");
        if (children instanceof ArrayNode array) {
            List<ObjectNode> nodes = new ArrayList<>();
            for (JsonNode child : array) {
                if (child.isObject()) {
                    nodes.add((ObjectNode) child);
                }
            }
            nodes.sort(Comparator.comparingInt(n -> n.path("order").asInt(0)));
            array.removeAll();
            nodes.forEach(array::add);
            nodes.forEach(this::normalizeOrder);
        }
    }

    /**
     * 递归解析节点及其子节点中的接口引用；解析失败时保留原值。
     */
    private void resolveReferences(ObjectNode node, Map<String, ApiEndpoint> endpoints, JsonNode openapi) {
        JsonNode api = node.get("api");
        if (api != null && api.isString()) {
            String raw = api.asString();
            if (raw.startsWith(REFERENCE_PREFIX)) {
                String name = raw.substring(REFERENCE_PREFIX.length()).trim();
                ObjectNode resolved = resolveOperation(name, endpoints, openapi);
                if (resolved != null) {
                    node.set("api", resolved);
                } else {
                    log.warn("[抽象组件] 接口引用保持原样：{}", raw);
                }
            }
        }

        JsonNode children = node.get("containers");
        if (children != null && children.isArray()) {
            for (JsonNode child : children) {
                if (child.isObject()) {
                    resolveReferences((ObjectNode) child, endpoints, openapi);
                }
            }
        }
    }

    /**
     * 把 {@code @ResourceReference:<name>} 解析为 {method, path, request, response}。
     * 任一环节失败返回 null，由调用方保留原值。
     */
    private ObjectNode resolveOperation(String name, Map<String, ApiEndpoint> endpoints, JsonNode openapi) {
        ApiEndpoint endpoint = endpoints.get(name);
        if (endpoint == null) {
            log.warn("[抽象组件] 接口引用未命中（端点不存在，或目标方法未标注 @ResourceReference）：{}", name);
            return null;
        }

        JsonNode operation = openapi
            .path("paths")
            .path(endpoint.path())
            .path(endpoint.method().toLowerCase(Locale.ROOT));
        if (!operation.isObject()) {
            log.warn("[抽象组件] OpenAPI 文档中找不到端点：{} {}", endpoint.method(), endpoint.path());
            return null;
        }

        ObjectNode resolved = mapper.createObjectNode();
        resolved.put("method", endpoint.method());
        resolved.put("path", endpoint.path());
        resolved.set("request", buildRequest(operation));
        resolved.set("response", buildResponse(operation));
        return resolved;
    }

    /**
     * 入参结构：合并 query/path/header 参数与 requestBody 的属性，统一为 object schema。
     */
    private ObjectNode buildRequest(JsonNode operation) {
        ObjectNode request = mapper.createObjectNode();
        ObjectNode schema = mapper.createObjectNode();
        ObjectNode properties = mapper.createObjectNode();
        ArrayNode required = mapper.createArrayNode();

        JsonNode body = operation.path("requestBody");
        if (body.isObject()) {
            JsonNode content = body.path("content");
            if (content.isObject() && !content.properties().isEmpty()) {
                Map.Entry<String, JsonNode> first = content.properties().iterator().next();
                request.put("content_type", first.getKey());
                JsonNode bodySchema = first.getValue().path("schema");
                if (bodySchema.isObject()) {
                    JsonNode bodyProperties = bodySchema.path("properties");
                    if (bodyProperties.isObject()) {
                        for (Map.Entry<String, JsonNode> entry : bodyProperties.properties()) {
                            properties.set(entry.getKey(), entry.getValue().deepCopy());
                        }
                    }
                    JsonNode bodyRequired = bodySchema.path("required");
                    if (bodyRequired.isArray()) {
                        for (JsonNode item : bodyRequired) {
                            if (item.isString()) {
                                required.add(item.asString());
                            }
                        }
                    }
                }
            }
        }

        JsonNode parameters = operation.path("parameters");
        if (parameters.isArray()) {
            for (JsonNode parameter : parameters) {
                if (!parameter.isObject()) {
                    continue;
                }
                String name = parameter.path("name").asString("");
                if (name.isEmpty()) {
                    continue;
                }
                JsonNode parameterSchema = parameter.path("schema");
                properties.set(name, parameterSchema.isObject()
                    ? parameterSchema.deepCopy()
                    : mapper.createObjectNode());
                if (parameter.path("required").asBoolean(false)) {
                    required.add(name);
                }
            }
            if (!request.has("content_type") && properties.size() > 0) {
                request.put("content_type", "application/json");
            }
        }

        schema.put("type", "object");
        schema.set("properties", properties);
        if (required.size() > 0) {
            schema.set("required", required);
        }
        request.set("schema", schema);
        return request;
    }

    /**
     * 出参结构：取首个 2xx（无则取第一个）响应的 content 与 schema。
     */
    private ObjectNode buildResponse(JsonNode operation) {
        ObjectNode response = mapper.createObjectNode();
        JsonNode responses = operation.path("responses");
        if (!responses.isObject() || responses.properties().isEmpty()) {
            response.put("status", "200");
            return response;
        }

        Map.Entry<String, JsonNode> chosen = null;
        for (Map.Entry<String, JsonNode> entry : responses.properties()) {
            if (entry.getKey().startsWith("2")) {
                chosen = entry;
                break;
            }
        }
        if (chosen == null) {
            chosen = responses.properties().iterator().next();
        }

        response.put("status", chosen.getKey());
        JsonNode content = chosen.getValue().path("content");
        if (content.isObject() && !content.properties().isEmpty()) {
            Map.Entry<String, JsonNode> media = content.properties().iterator().next();
            response.put("content_type", media.getKey());
            JsonNode schema = media.getValue().path("schema");
            if (schema.isObject()) {
                response.set("schema", schema.deepCopy());
            }
        }
        return response;
    }

    /**
     * 建立「引用标识 → 端点」索引，只收 {@link ResourceReference} 标注过的方法。
     * 未标注的方法不会进入索引，因此不可被装配描述引用。
     */
    Map<String, ApiEndpoint> annotatedEndpoints() {
        Map<String, ApiEndpoint> index = new TreeMap<>();
        RequestMappingHandlerMapping mapping = handlerMappings.getIfAvailable();
        if (mapping == null) {
            log.warn("[抽象组件] 请求处理映射不可用，接口引用索引为空");
            return index;
        }

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : mapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handler = entry.getValue();

            ResourceReference annotation = handler.getMethodAnnotation(ResourceReference.class);
            if (annotation == null) {
                continue;
            }

            String name = annotation.name() == null ? "" : annotation.name().trim();
            if (name.isEmpty()) {
                log.warn("[抽象组件] @ResourceReference 未声明 name，已跳过：{}", handler.getShortLogMessage());
                continue;
            }

            Set<String> paths = info.getPatternValues();
            if (paths.isEmpty()) {
                log.warn("[抽象组件] @ResourceReference 标注的方法没有映射路径，已跳过：{}", name);
                continue;
            }
            String path = paths.stream().sorted().findFirst().orElseThrow();

            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            String httpMethod = methods.stream()
                .map(Enum::name)
                .sorted()
                .findFirst()
                .orElse("GET");
            if (methods.isEmpty()) {
                log.warn("[抽象组件] @ResourceReference 标注的方法未限定 HTTP 方法，按 GET 处理：{}", name);
            }

            ApiEndpoint previous = index.put(name, new ApiEndpoint(httpMethod, path));
            if (previous != null) {
                log.warn("[抽象组件] 重复的 @ResourceReference 标识「{}」，以 {} {} 覆盖 {} {}",
                    name, httpMethod, path, previous.method(), previous.path());
            }
        }
        return index;
    }

    private JsonNode readJson(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            log.warn("[抽象组件] 固定路径不存在：{}", path);
            return null;
        }
        try (InputStream stream = resource.getInputStream()) {
            return mapper.readTree(stream);
        } catch (Exception e) {
            log.warn("[抽象组件] 固定路径读取失败：{}（{}）", path, e.getMessage());
            return null;
        }
    }

    /**
     * 对外接口响应：{types, pages}。注册表为空时为两个空数组。
     */
    public record ComponentResponse(List<JsonNode> types, List<JsonNode> pages) {
    }
}
