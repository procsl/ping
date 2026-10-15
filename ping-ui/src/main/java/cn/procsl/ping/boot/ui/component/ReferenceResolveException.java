package cn.procsl.ping.boot.ui.component;

/**
 * {@code @ResourceReference} 解析异常基类。
 *
 * <p>编译阶段解析 OpenAPI 描述失败（operationId 重复、无成功响应等）时抛出，
 * 异常信息包含具体原因，便于快速定位声明错误。
 *
 * @see ReferenceNotFoundException
 */
public class ReferenceResolveException extends RuntimeException {

    public ReferenceResolveException(String message) {
        super(message);
    }

    public ReferenceResolveException(String message, Throwable cause) {
        super(message, cause);
    }
}
