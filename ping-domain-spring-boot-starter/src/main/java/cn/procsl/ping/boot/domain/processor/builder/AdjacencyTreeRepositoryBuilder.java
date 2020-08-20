package cn.procsl.ping.boot.domain.processor.builder;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.domain.processor.AbstractRepositoryBuilder;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

public class AdjacencyTreeRepositoryBuilder extends AbstractRepositoryBuilder {

    /**
     * 获取支持的repository的class对象
     *
     * @return 返回支持的Repository 对象
     */
    @Override
    protected Class<AdjacencyTreeRepository> getSupportRepositoryClass() {
        return AdjacencyTreeRepository.class;
    }

    /**
     * 构建接口元素
     *
     * @param entity   当前的实体
     * @param roundEnv 当前编译器上下文
     * @return 返回接口标识
     */
    @Override
    public TypeName build(Element entity, RoundEnvironment roundEnv) {
        return null;
    }
}
