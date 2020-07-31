package cn.procsl.ping.boot.domain.processor;

import com.squareup.javapoet.TypeName;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

/**
 * Repository构建器接口
 *
 * @author procsl
 * @date 2020/05/22
 */
public interface RepositoryBuilder {

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
    TypeName build(Element entity, RoundEnvironment roundEnv);

}
