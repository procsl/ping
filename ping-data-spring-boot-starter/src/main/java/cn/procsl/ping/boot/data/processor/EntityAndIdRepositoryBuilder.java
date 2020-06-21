package cn.procsl.ping.boot.data.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;

/**
 * @author procsl
 * @date 2020/05/23
 */
public abstract class EntityAndIdRepositoryBuilder extends AbstractRepositoryBuilder {

    @Override
    public TypeName build(Element entity, RoundEnvironment roundEnv) {
        ClassName repositoryType = ClassName.get(this.getSupportRepositoryClass());

        TypeName idType = createIdType(entity);
        if (idType == null) {
            return null;
        }

        TypeName entityType = createEntityType(entity);
        return ParameterizedTypeName.get(repositoryType, entityType, idType);
    }
}
