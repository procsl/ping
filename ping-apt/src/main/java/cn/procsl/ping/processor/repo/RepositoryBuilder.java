package cn.procsl.ping.processor.repo;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Repository构建器接口
 *
 * @author procsl
 * @date 2020/05/22
 */
public interface RepositoryBuilder {

    String processor = "META-INF/processor.config";

    String prefix = "processor.repository.prefix";

    String pageName = "processor.repository.package.name";

    String include = "processor.repository.includes";

    /**
     * 初始化接口
     *
     * @param processingEnv 编译器上下文
     * @param configFinder  获取配置
     */
    void init(ProcessingEnvironment processingEnv, Function<String, String> configFinder);

    /**
     * 是否支持生成该类
     *
     * @param className 待测试的Repository类型名称
     * @return 返回是否支持该Repository
     */
    boolean support(String className);

    /**
     * 构建接口元素
     *
     * @param entity   当前的实体
     * @param roundEnv 当前编译器上下文
     * @return 返回接口标识
     */
    Map<String, List<TypeMirror>> generator(TypeElement entity, RoundEnvironment roundEnv);

    /**
     * 是否独立实现, 不同时继承多个(某些接口有冲突的情况下)
     *
     * @return 如果独立创建, 则返回true
     */
    boolean isSingleton();

    /**
     * 可创建的储存库名称
     *
     * @return 名称
     */
    Collection<String> getName();
}
