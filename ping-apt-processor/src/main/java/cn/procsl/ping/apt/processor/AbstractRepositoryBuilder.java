package cn.procsl.ping.apt.processor;

import com.squareup.javapoet.TypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.persistence.Id;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.function.Function;

import static com.squareup.javapoet.TypeName.get;

/**
 * @author procsl
 * @date 2020/06/21
 */
public abstract class AbstractRepositoryBuilder implements RepositoryBuilder {

    private static final Logger log = LoggerFactory.getLogger(AbstractRepositoryBuilder.class);

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
        log.debug("Init builder:{}", this.getClass().getName());
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
    protected abstract Class<?> getSupportRepositoryClass() throws ClassNotFoundException;

    @Override
    public boolean support(String className) throws ClassNotFoundException {
        return this.getSupportRepositoryClass().getName().equals(className);
    }

    /**
     * 创建ID的参数化类型
     *
     * @param entity 对应的实体
     * @return 实体中ID对应的类型, 如果id未被标记, 则返回null
     */
    public TypeName createIdType(TypeElement entity) {
        List<? extends Element> elements = entity.getEnclosedElements();
        for (Element element : elements) {
            Id id = element.getAnnotation(Id.class);
            if (id != null) {
                return TypeName.get(element.asType());
            }
        }
        this.processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING, "Not fount @Id annotation", entity);
        log.warn("Not fount annotation @javax.persistence.Id");
        return null;
    }

    /**
     * 创建实体对应的类型
     *
     * @param entity 实体
     * @return 实体对应的类型描述
     */
    public TypeName createEntityType( TypeElement entity) {
        return get(entity.asType());
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
    public String getName() {
        return "";
    }

    /**
     * 用于统一抓取配置
     *
     * @param prop 配置名称
     * @return 返回配置
     */
    protected String getConfig( String prop) {
        return configFinder.apply(prop);
    }
}
