package cn.procsl.ping.boot.ui.component;

/**
 * {@code ref} → UUID 解析异常基类。
 *
 * <p>编译阶段解析 input/output 组件的路径式 {@code ref} 失败时抛出
 * （格式非法、跨 operation、字段不存在等），异常信息包含具体 {@code ref} 路径，
 * 便于快速定位声明错误。
 *
 * @see RefNotFoundException
 */
public class RefResolveException extends RuntimeException {

    public RefResolveException(String message) {
        super(message);
    }

    public RefResolveException(String message, Throwable cause) {
        super(message, cause);
    }
}
