package cn.procsl.ping.apt.neo.repository;

import cn.procsl.ping.apt.AptUtils;
import cn.procsl.ping.apt.SimpleAnnotationValueVisitor;
import cn.procsl.ping.apt.SimpleElementVisitor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.List;
import java.util.Set;

final class RepositoryCreatorsVisitor implements TargetElementProcessor {

    final String repository_creators_class = "cn.procsl.ping.boot.jpa.support.RepositoryCreators";
    final RepositoryCreatorVisitor repositoryCreatorsBuilder = new RepositoryCreatorVisitor();
    final SimpleElementVisitor<TypeElement, Object> visitor = SimpleElementVisitor.ofVisitType((e, s) -> e);
    final SimpleAnnotationValueVisitor<List<? extends AnnotationValue>, Object> av = SimpleAnnotationValueVisitor.ofVisitArray((e, s) -> e);
    final SimpleAnnotationValueVisitor<AnnotationMirror, Object> am = SimpleAnnotationValueVisitor.ofVisitAnnotation((e, s) -> e);

    @Override
    public void build(ProcessingEnvironment env, RoundEnvironment roundEnv,
                      TypeElement annotation, Element target) {
        TypeElement entity = target.accept(visitor, null);
        if (entity == null) {
            return;
        }

        var mirrors = entity.getAnnotationMirrors();
        var mirror = AptUtils.findAnnotation(env.getElementUtils(), mirrors,
            this.repository_creators_class);
        if (mirror == null) {
            return;
        }

        var values = AptUtils.findAnnotationValueOrDefaultValue(env.getElementUtils(), mirror, "value");
        if (values == null) {
            return;
        }

        var arrays = values.accept(av, null);
        if (arrays == null || arrays.isEmpty()) {
            return;
        }

        for (AnnotationValue array : arrays) {
            AnnotationMirror value = array.accept(am, null);
            if (value == null) {
                continue;
            }
            this.repositoryCreatorsBuilder.build(env, roundEnv, entity, value);
        }

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(repository_creators_class);
    }


}
