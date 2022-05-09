package cn.procsl.ping.processor.repo;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author procsl
 * @date 2020/06/21
 */
public abstract class EntityRepositoryBuilder extends AbstractRepositoryBuilder {

    @Override
    public Map<String, List<TypeMirror>> generator(TypeElement entity, RoundEnvironment roundEnv) {
        return Map.of(this.getSupportRepositoryClass(), Collections.singletonList(entity.asType()));
    }
}
