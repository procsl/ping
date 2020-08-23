package cn.procsl.ping.boot.domain.processor;

import com.squareup.javapoet.TypeName;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.function.Function;

/**
 * Repository构建器接口
 *
 * @author procsl
 * @date 2020/05/22
 */
public interface RepositoryBuilder {

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
    TypeName build(TypeElement entity, RoundEnvironment roundEnv);

    /**
     * 是否独立实现, 不同时继承多个(某些接口有冲突的情况下)
     *
     * @return 如果独立创建, 则返回true
     */
    boolean isSingleton();
}
