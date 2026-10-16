package cn.procsl.ping.boot.ui.component;

import java.util.List;

/**
 * 引用断链异常。
 *
 * <p>编译阶段解析引用时，若目标无法定位则抛出，异常信息包含完整引用路径：
 * <ul>
 *   <li>operation 级断链 —— {@code openapi://<operationId>} 在 OpenAPI 文档中不存在，
 *       附带全部可用 operationId 列表，便于修正引用</li>
 *   <li>字段路径级断链 —— {@code openapi://<operationId>/<request|response>/<path>}
 *       无法定位到字段（operation / side / 字段 / items 缺失）</li>
 *   <li>OpenAPI schema 内部 {@code $ref}（如 {@code #/components/schemas/X}）目标不存在</li>
 *   <li>defaults / overrides 定位的目标字段不存在</li>
 * </ul>
 *
 * @see OperationRefResolver
 * @see RefResolver
 * @see SchemaRefExpander
 * @see DefaultResolver
 */
public class RefNotFoundException extends ComposeCompileException {

    /** 完整引用路径（operation 级为 {@code openapi://<operationId>}，字段级为完整路径或 schema $ref） */
    private final String ref;

    /** operation 级断链时的可用 operationId 列表；非 operation 级断链为 null */
    private final List<String> availableOperationIds;

    /** 字段路径级断链（包含 defaults / overrides 目标字段与 schema 内部 $ref） */
    public RefNotFoundException(String ref, String reason) {
        super("Ref '" + ref + "' not found: " + reason);
        this.ref = ref;
        this.availableOperationIds = null;
    }

    /** operation 级断链（附带可用 operationId 列表） */
    public RefNotFoundException(String operationId, List<String> availableOperationIds) {
        super("Ref 'openapi://" + operationId + "' not found in OpenAPI document, available operationIds: "
                + availableOperationIds);
        this.ref = "openapi://" + operationId;
        this.availableOperationIds = List.copyOf(availableOperationIds);
    }

    public String getRef() {
        return ref;
    }

    /** @return 可用 operationId 列表；非 operation 级断链时为 null */
    public List<String> getAvailableOperationIds() {
        return availableOperationIds;
    }
}
