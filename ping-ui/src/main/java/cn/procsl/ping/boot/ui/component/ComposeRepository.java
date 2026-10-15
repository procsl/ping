package cn.procsl.ping.boot.ui.component;

import tools.jackson.core.JacksonException;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * compose.json 仓储。
 *
 * <p>负责扫描 classpath 下所有 {@code META-INF/ping/ui/compose.json} 文件，
 * 解析、校验并合并为 {@link Compose} 聚合根。
 *
 * <p>设计决策：
 * <ul>
 *   <li>compose.json 内容为 JSON 数组，不需要 {@code type: "app"} 包装</li>
 *   <li>空数组 {@code []} 合法，表示该模块无 UI 组件</li>
 *   <li>顶层类型仅允许 {@code operation} 和 {@code function}</li>
 *   <li>多个模块的 compose.json 合并为一个列表</li>
 * </ul>
 */
public class ComposeRepository {

    private static final String COMPOSE_LOCATION = "classpath*:META-INF/ping/ui/compose.json";

    private static final Set<String> VALID_ROOT_TYPES = Set.of("operation", "function");

    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourcePatternResolver;

    public ComposeRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
    }

    /**
     * 加载所有模块的 compose.json 并合并为 Compose 聚合根。
     *
     * @return Compose 聚合根，包含所有 operation/function 子节点
     * @throws ComposeParseException 当 JSON 解析失败或顶层类型非法时
     */
    public Compose loadAll() {
        ArrayNode result = objectMapper.createArrayNode();
        List<Resource> resources = findComposeResources();

        for (Resource resource : resources) {
            ArrayNode items = parseComposeFile(resource);
            if (items != null) {
                items.forEach(result::add);
            }
        }

        return new Compose(result);
    }

    private List<Resource> findComposeResources() {
        try {
            Resource[] resources = resourcePatternResolver.getResources(COMPOSE_LOCATION);
            return List.of(resources);
        } catch (IOException e) {
            throw new ComposeParseException(COMPOSE_LOCATION, null,
                    "Failed to scan compose.json resources: " + e.getMessage(), e);
        }
    }

    private ArrayNode parseComposeFile(Resource resource) {
        String filePath = getResourcePath(resource);

        try (InputStream is = resource.getInputStream()) {
            JsonNode root = objectMapper.readTree(is);

            if (!root.isArray()) {
                throw new ComposeParseException(filePath, null,
                        "compose.json root must be a JSON array, got: " + root.getNodeType());
            }

            ArrayNode items = (ArrayNode) root;
            for (int i = 0; i < items.size(); i++) {
                validateTopLevelType(items.get(i), filePath, i);
            }

            return items;
        } catch (JacksonException e) {
            TokenStreamLocation loc = e.getLocation();
            Integer line = loc != null && loc.getLineNr() > 0 ? loc.getLineNr() : null;
            throw new ComposeParseException(filePath, line, e.getOriginalMessage(), e);
        } catch (IOException e) {
            throw new ComposeParseException(filePath, null,
                    "Failed to read compose.json: " + e.getMessage(), e);
        }
    }

    private void validateTopLevelType(JsonNode item, String filePath, int index) {
        if (!item.isObject()) {
            throw new ComposeParseException(filePath, null,
                    "Array element at index " + index + " must be an object, got: " + item.getNodeType());
        }

        JsonNode typeNode = item.get("type");
        if (typeNode == null || !typeNode.isTextual()) {
            throw new ComposeParseException(filePath, null,
                    "Array element at index " + index + " missing 'type' field");
        }

        String type = typeNode.asText();
        if (!VALID_ROOT_TYPES.contains(type)) {
            throw new ComposeParseException(filePath, null,
                    "Array element at index " + index + " has invalid type '" + type
                            + "', allowed: " + VALID_ROOT_TYPES);
        }
    }

    private String getResourcePath(Resource resource) {
        try {
            return resource.getURI().toString();
        } catch (IOException e) {
            return resource.getFilename() != null ? resource.getFilename() : "unknown";
        }
    }
}
