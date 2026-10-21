package cn.procsl.ping.boot.ui.repository;

import cn.procsl.ping.boot.ui.exception.ComposeParseException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import tools.jackson.core.JacksonException;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * compose.json 仓储：只做数据访问——扫描、解析、多文件合并（design D9）。
 *
 * <p>扫描 classpath 下所有模块的 {@code META-INF/ping/ui/compose.json}（精确文件名，
 * 跨 jar 聚合，无通配模式），解析为节点列表返回；类型分派与结构校验归编译器。
 *
 * <p>解析级错误（JSON 非法、顶层非数组、元素非对象）抛 {@link ComposeParseException}，
 * 消息含文件路径与行号（声明来源）。
 */
public class ComposeRepository {

    private static final String COMPOSE_LOCATION = "classpath*:META-INF/ping/ui/compose.json";

    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourcePatternResolver;

    public ComposeRepository(ObjectMapper objectMapper) {
        this(objectMapper, new PathMatchingResourcePatternResolver());
    }

    ComposeRepository(ObjectMapper objectMapper, ResourcePatternResolver resourcePatternResolver) {
        this.objectMapper = objectMapper;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载所有模块的 compose.json 并合并为节点列表（多文件按扫描顺序拼接）。
     *
     * @return 不可变节点列表；无任何 compose.json 时为空列表
     * @throws ComposeParseException 当扫描失败或任一文件解析非法
     */
    public List<JsonNode> loadAll() {
        try {
            return merge(List.of(resourcePatternResolver.getResources(COMPOSE_LOCATION)));
        } catch (IOException e) {
            throw new ComposeParseException(COMPOSE_LOCATION, null,
                "Failed to scan compose.json resources", e);
        }
    }

    /**
     * 合并多个 compose.json 资源（包级可见，供单测注入多资源覆盖合并行为）。
     */
    List<JsonNode> merge(List<Resource> resources) {
        List<JsonNode> result = new ArrayList<>();
        for (Resource resource : resources) {
            String filePath = resourcePath(resource);
            JsonNode root = parse(resource, filePath);

            if (!root.isArray()) {
                throw new ComposeParseException(filePath, null,
                    "compose.json root must be a JSON array, got: " + root.getNodeType());
            }

            for (int i = 0; i < root.size(); i++) {
                JsonNode item = root.get(i);
                if (!item.isObject()) {
                    throw new ComposeParseException(filePath, null,
                        "Array element at index " + i + " must be an object, got: " + item.getNodeType());
                }
                result.add(item);
            }
        }
        return List.copyOf(result);
    }

    private JsonNode parse(Resource resource, String filePath) {
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readTree(is);
        } catch (JacksonException e) {
            TokenStreamLocation loc = e.getLocation();
            Integer line = loc != null && loc.getLineNr() > 0 ? loc.getLineNr() : null;
            throw new ComposeParseException(filePath, line, e.getOriginalMessage(), e);
        } catch (IOException e) {
            throw new ComposeParseException(filePath, null,
                "Failed to read compose.json", e);
        }
    }

    private String resourcePath(Resource resource) {
        try {
            return resource.getURI().toString();
        } catch (IOException e) {
            return resource.getFilename() != null ? resource.getFilename() : "unknown";
        }
    }
}
