package cn.procsl.ping.boot.data.processor.impl;

import cn.procsl.ping.boot.data.processor.RepositoryBuilder;
import com.squareup.javapoet.TypeName;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

/**
 * 创建 {@link org.springframework.data.jpa.repository.JpaRepository}
 *
 * @author procsl
 * @date 2020/05/23
 */
public class JpaRepositoryBuilder implements RepositoryBuilder {
    @Override
    public boolean support(String className) {
        return JpaRepository.class.getName().equals(className);
    }

    @Override
    public TypeName build(Element entity, RoundEnvironment roundEnv) {
        return null;
    }
}
