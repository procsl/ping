package cn.procsl.ping.boot.jpa.support.query.ast;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 解析一个pojo对象, 解析字段与get方法, 并返回子节点
 * 如果是容器类型则解析容器元素的类型
 */
public interface FieldDescription {

    /**
     * 获取父节点信息
     */
    @JsonIgnore
    Optional<FieldDescription> getParentField();

    /**
     * 是否是根节点
     */
    default boolean isRoot() {
        return getParentField().isEmpty();
    }

    String getFieldName();

    Class<?> getType();

    List<FieldDescription> getChildren();

    List<Annotation> getAnnotations();

    default <T extends Annotation> List<T> findAnnotations(Class<T> clazz) {
        return ClassUtils.filter(this.getAnnotations(), clazz);
    }


    default <T extends Annotation> T findMargeAnnotationOrDefault(Class<T> clazz, Supplier<T> instance) {
        return ClassUtils.createMargeAnnotationOrDefault(clazz, this.getAnnotations(), instance);
    }

    default <T extends Annotation> T findMargeAnnotation(Class<T> clazz) {
        return ClassUtils.createMargeAnnotationOrDefault(clazz, this.getAnnotations(), () -> null);
    }

}
