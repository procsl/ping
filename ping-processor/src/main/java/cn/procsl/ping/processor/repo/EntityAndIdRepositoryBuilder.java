package cn.procsl.ping.processor.repo;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/05/23
 */
public abstract class EntityAndIdRepositoryBuilder extends AbstractRepositoryBuilder {

    @Override
    public Map<String, List<TypeMirror>> generator(TypeElement entity, RoundEnvironment roundEnv) {
        TypeMirror idType = findIdType(entity);
        if (idType == null) {
            return null;
        }
        return Map.of(this.getSupportRepositoryClass(), Arrays.asList(entity.asType(), idType));
    }
}
