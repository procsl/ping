package cn.procsl.ping.boot.system.domain.ac;

import java.util.Map;
import java.util.Optional;

public interface AttributeContainer {
    /**
     * 获取当前容器的类型/命名空间（如 "ADMIN", "order", "EXPORT"）
     */
    String getType();

    /**
     * 获取全量的属性字典
     */
    Map<String, Object> getAttributes();

    /**
     * 便捷方法：获取单个属性
     */
    default Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(getAttributes().get(key));
    }

    /**
     * 支持泛型的属性获取方法，方便表达式引擎或业务代码类型转换
     */
    default <T> T getAttributeAs(String key, Class<T> type) {
        Object value = getAttributes().get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        throw new IllegalArgumentException("属性 [" + key + "] 的类型不是 " + type.getName());
    }
}
