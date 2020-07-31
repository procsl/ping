package cn.procsl.ping.boot.domain.processor;

import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Element;
import javax.persistence.Id;
import java.util.List;

import static com.squareup.javapoet.TypeName.get;

/**
 * @author procsl
 * @date 2020/06/21
 */
public abstract class AbstractRepositoryBuilder implements RepositoryBuilder {

    /**
     * 获取支持的repository的class对象
     *
     * @return 返回支持的Repository 对象
     */
    protected abstract Class getSupportRepositoryClass();

    @Override
    public boolean support(String className) {
        return this.getSupportRepositoryClass().getName().equals(className);
    }

    /**
     * 创建ID的参数化类型
     *
     * @param entity 对应的实体
     * @return 实体中ID对应的类型, 如果id未被标记, 则返回null
     */
    public static TypeName createIdType(Element entity) {
        List<? extends Element> elements = entity.getEnclosedElements();
        for (Element element : elements) {
            Id id = element.getAnnotation(Id.class);
            if (id != null) {
                return TypeName.get(element.asType());
            }
        }
        return null;
    }

    /**
     * 创建实体对应的类型
     *
     * @param entity 实体
     * @return 实体对应的类型描述
     */
    public static TypeName createEntityType(Element entity) {
        return get(entity.asType());
    }

}
