package cn.procsl.ping.boot.ui.component;

import java.util.List;

/**
 * {@code @ResourceReference} 引用目标不存在异常。
 *
 * <p>compose.json 中声明的 {@code @ResourceReference:<operationId>} 在 OpenAPI 文档中
 * 找不到对应 operation 时抛出，异常信息包含文档中全部可用的 operationId 列表，
 * 便于修正引用。
 */
public class ReferenceNotFoundException extends ReferenceResolveException {

    private final String operationId;
    private final List<String> availableOperationIds;

    public ReferenceNotFoundException(String operationId, List<String> availableOperationIds) {
        super("ResourceReference '" + operationId + "' not found in OpenAPI document, available operationIds: "
                + availableOperationIds);
        this.operationId = operationId;
        this.availableOperationIds = List.copyOf(availableOperationIds);
    }

    public String getOperationId() {
        return operationId;
    }

    public List<String> getAvailableOperationIds() {
        return availableOperationIds;
    }
}
