package cn.procsl.ping.boot.ui.component;

/**
 * 默认值 / 覆盖值解析异常。
 *
 * <p>编译阶段把 operation 级 {@code defaults} 或容器级 {@code overrides} 合并到
 * operation 字段时，若目标字段不存在或覆盖值类型与 schema 声明不一致则抛出。
 *
 * @see DefaultResolver
 */
public class DefaultResolveException extends RefResolveException {

    public DefaultResolveException(String message) {
        super(message);
    }

    public DefaultResolveException(String message, Throwable cause) {
        super(message, cause);
    }
}
