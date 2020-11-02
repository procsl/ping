package cn.procsl.ping.processor.repository.processor;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author procsl
 * @date 2020/06/21
 */
public abstract class EntityRepositoryBuilder extends AbstractRepositoryBuilder {

    @Override
    public TypeMirror generator(TypeElement entity, RoundEnvironment roundEnv) {
//        ClassName repositoryType = ClassName.get(this.getSupportRepositoryClass());
//        TypeName entityType = this.createEntityType(entity);
//        ParameterizedTypeName.get(repositoryType, entityType);
        return null;
    }
}
