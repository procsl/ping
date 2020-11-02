package cn.procsl.ping.processor.repository.processor;

import com.squareup.javapoet.TypeName;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author procsl
 * @date 2020/05/23
 */
public abstract class EntityAndIdRepositoryBuilder extends AbstractRepositoryBuilder {

    @Override
    public TypeMirror generator(TypeElement entity, RoundEnvironment roundEnv) {
//        ClassName repositoryType = ClassName.get(this.getSupportRepositoryClass());

        TypeName idType = createIdType(entity);
        if (idType == null) {
            return null;
        }

        TypeName entityType = createEntityType(entity);
//        ParameterizedTypeName.get(repositoryType, entityType, idType);
        return null;
    }
}
