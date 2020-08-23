package cn.procsl.ping.boot.domain.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * @author procsl
 * @date 2020/06/21
 */
public abstract class EntityRepositoryBuilder extends AbstractRepositoryBuilder {

    @Override
    public TypeName build(TypeElement entity, RoundEnvironment roundEnv) {
        ClassName repositoryType = ClassName.get(this.getSupportRepositoryClass());
        TypeName entityType = this.createEntityType(entity);
        return ParameterizedTypeName.get(repositoryType, entityType);
    }
}
