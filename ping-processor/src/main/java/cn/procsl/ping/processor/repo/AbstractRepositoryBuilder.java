package cn.procsl.ping.processor.repo;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.persistence.Id;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author procsl
 * @date 2020/06/21
 */
public abstract class AbstractRepositoryBuilder implements RepositoryBuilder {

    /**
     * 获取编译器工具
     */
    protected ProcessingEnvironment processingEnvironment;

    /**
     * 用于获取配置
     */
    private Function<String, String> configFinder;

    /**
     * 初始化接口
     *
     * @param processingEnv 编译器环境
     */
    @Override
    public final void init(ProcessingEnvironment processingEnv, Function<String, String> configFinder) {
        this.processingEnvironment = processingEnv;
        this.configFinder = configFinder;
        innerInit();
        this.processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, "Init builder: " + this.getClass().getName());
    }

    /**
     * 内部初始化
     */
    protected void innerInit() {
    }

    /**
     * 获取支持的repository的class对象
     *
     * @return 返回支持的Repository 对象
     */
    protected abstract String getSupportRepositoryClass();

    @Override
    public boolean support(String className) {
        return this.getSupportRepositoryClass().equals(className);
    }

    /**
     * 创建ID的参数化类型
     *
     * @param entity 对应的实体
     * @return 实体中ID对应的类型, 如果id未被标记, 则返回null
     */
    public TypeMirror findIdType(TypeElement entity) {
        List<? extends Element> elements = entity.getEnclosedElements();
        for (Element element : elements) {
            Id id = element.getAnnotation(Id.class);
            if (id != null) {
                return element.asType();
            }
        }

        TypeMirror superClass = entity.getSuperclass();
        if (superClass.toString().startsWith("org.springframework.data.jpa.domain.AbstractPersistable") && superClass instanceof DeclaredType) {
            List<? extends TypeMirror> arguments = ((DeclaredType) superClass).getTypeArguments();
            if (!arguments.isEmpty()) {
                return arguments.get(0);
            }
        }
//        ((DeclaredType)entity.getSuperclass()).getTypeArguments();

        this.processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING, "Not fount @Id annotation", entity);
        return null;
    }


    /**
     * 是否独立实现, 不同时继承多个(某些接口有冲突的情况下)
     *
     * @return 如果独立创建, 则返回true
     */
    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public Collection<String> getName() {
        return Collections.emptyList();
    }

    /**
     * 用于统一抓取配置
     *
     * @param prop 配置名称
     * @return 返回配置
     */
    @SuppressWarnings("unused")
    protected String getConfig(String prop) {
        return configFinder.apply(prop);
    }
}
