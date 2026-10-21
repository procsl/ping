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
 * OpenAPI 文档仓储：只做数据访问——扫描打包期导出的 {@code META-INF/ping/openapi-<module>.json}
 *（多文件，文件名 {@code openapi-*.json}），解析为完整文档列表（design D7）。
 *
 * <p>完整文档（含 {@code paths} 与 {@code components}）整体交给
 * {@link cn.procsl.ping.boot.ui.compiler.CompileContext}：operationId 索引与冲突检测在
 * CompileContext 构建期完成，{@code $ref} 展开按 operation 的来源文档取 components（文档隔离）。
 */
public class OpenApiRepository {

    private static final String OPENAPI_LOCATION = "classpath*:META-INF/ping/openapi-*.json";

    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourcePatternResolver;

    public OpenApiRepository(ObjectMapper objectMapper) {
        this(objectMapper, new PathMatchingResourcePatternResolver());
    }

    OpenApiRepository(ObjectMapper objectMapper, ResourcePatternResolver resourcePatternResolver) {
        this.objectMapper = objectMapper;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 扫描所有模块的 openapi-*.json，解析为完整文档列表（按扫描顺序）。
     *
     * @return 不可变文档列表；无任何 openapi 文件时为空列表
     * @throws ComposeParseException 当扫描失败或任一文件解析非法（消息含文件来源）
     */
    public List<JsonNode> loadDocuments() {
        try {
            return parseAll(List.of(resourcePatternResolver.getResources(OPENAPI_LOCATION)));
        } catch (IOException e) {
            throw new ComposeParseException(OPENAPI_LOCATION, null,
                "Failed to scan openapi-*.json resources", e);
        }
    }

    /**
     * 解析多个 openapi 文档资源（包级可见，供单测注入多资源覆盖合并行为）。
     */
    List<JsonNode> parseAll(List<Resource> resources) {
        List<JsonNode> documents = new ArrayList<>();
        for (Resource resource : resources) {
            String filePath = resourcePath(resource);
            JsonNode doc = parse(resource, filePath);

            if (!doc.isObject()) {
                throw new ComposeParseException(filePath, null,
                    "openapi document must be a JSON object, got: " + doc.getNodeType());
            }
            documents.add(doc);
        }
        return List.copyOf(documents);
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
                "Failed to read openapi document", e);
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
