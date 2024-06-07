package cn.procsl.ping.apt.noe.repository;

import cn.procsl.ping.apt.AptUtils;
import cn.procsl.ping.apt.SimpleElementVisitor;
import com.squareup.javapoet.TypeSpec;
import lombok.SneakyThrows;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.procsl.ping.apt.SimpleAnnotationValueVisitor.ofVisitString;
import static cn.procsl.ping.apt.SimpleElementVisitor.ofVisitPackage;
import static cn.procsl.ping.apt.SimpleElementVisitor.ofVisitType;
import static cn.procsl.ping.apt.SimpleTypeVisitor.ofVisitDeclared;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

final class RepositoryCreatorVisitor implements TargetElementProcessor {

    final String repository_creator_class = "cn.procsl.ping.boot.jpa.support.RepositoryCreator";
    final String entity_class = "jakarta.persistence.Entity";
    final String id = "jakarta.persistence.Id";
    final String type_spec = "com.squareup.javapoet.TypeSpec";
    final SimpleElementVisitor<TypeElement, Object> visitor = SimpleElementVisitor.ofVisitType((e, s) -> e);

    @Override
    public void build(ProcessingEnvironment env, RoundEnvironment roundEnv,
                      TypeElement anno, Element target) {

        TypeElement entity = target.accept(visitor, null);
        if (entity == null) {
            return;
        }

        if (check(env, entity)) return;

        var mirrors = entity.getAnnotationMirrors();
        var annotationMirror = AptUtils.findAnnotation(env.getElementUtils(), mirrors, this.repository_creator_class);
        this.build(env, roundEnv, entity, annotationMirror);
    }

    private boolean check(ProcessingEnvironment env, TypeElement entity) {
        var messager = env.getMessager();
        if (!AptUtils.isAvailable(entity_class)) {
            messager.printMessage(WARNING, "class_path环境找不到注解: " + entity, entity);
            return true;
        }

        if (!AptUtils.isAvailable(type_spec)) {
            messager.printMessage(ERROR, "class_path环境找不到注解: " + type_spec);
            return true;
        }
        return false;
    }

    public void build(ProcessingEnvironment env, RoundEnvironment roundEnv, TypeElement entity, AnnotationMirror annotationMirror) {
        if (check(env, entity)) return;

        String className = this.createRepositoryClassName(env.getElementUtils(), entity, annotationMirror);
        String packageName = this.createRepositoryPackageName(env.getElementUtils(), entity, annotationMirror);
        TypeMirror idMirror = this.findIdAnnotationMirror(env.getElementUtils(), entity, id);

        var builder = TypeSpec.interfaceBuilder(className).addModifiers(Modifier.PUBLIC).build();

        AptUtils.writer(env.getMessager(), env.getFiler(), packageName, builder);
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(repository_creator_class);
    }

    private String createRepositoryClassName(Elements utils, TypeElement entity, AnnotationMirror repo) {

        AnnotationValue annotation = AptUtils.findAnnotationValue(utils, repo, "repositoryName");
        String name = null;
        if (annotation != null) {
            name = annotation.accept(ofVisitString((s, a) -> s), null);
        }
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return entity.getSimpleName().toString() + "Repository";
    }

    private String createRepositoryPackageName(Elements utils, TypeElement entity, AnnotationMirror repo) {
        AnnotationValue annotation = AptUtils.findAnnotationValue(utils, repo, "packageName");
        String name = null;
        if (annotation != null) {
            name = annotation.accept(ofVisitString((s, a) -> s), null);
        }
        if (name != null && !name.isEmpty()) {
            return name;
        }
        var packageType = entity.getEnclosingElement().accept(ofVisitPackage((s, p) -> s), null);
        name = packageType.getQualifiedName().toString();
        return name + ".repository";
    }

    private TypeMirror findIdAnnotationMirror(Elements utils, TypeElement entity, String id) {
        List<? extends Element> elements = entity.getEnclosedElements();
        for (Element element : elements) {
            List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
            AnnotationMirror mirror = AptUtils.findAnnotation(utils, mirrors, id);
            if (mirror != null) {
                return element.asType();
            }
        }

        TypeMirror superClass = entity.getSuperclass();
        if (superClass == null) {
            return null;
        }

        DeclaredType superType = superClass.accept(ofVisitDeclared((e, a) -> e), null);
        if (superType == null) {
            return null;
        }

        TypeElement typeElement = superType.asElement().accept(ofVisitType((e, a) -> e), null);
        if (typeElement == null) {
            return null;
        }

        final String abstract_persistable = "org.springframework.data.jpa.domain.AbstractPersistable";

        Name className = typeElement.getQualifiedName();
        var ap = utils.getName(abstract_persistable);

        if (ap.equals(className)) {
            List<? extends TypeMirror> arguments = superType.getTypeArguments();
            if (!arguments.isEmpty()) {
                return arguments.getFirst();
            }
        }

        final String persistable = "org.springframework.data.domain.Persistable";
        var p = utils.getName(persistable);
        if (p.equals(className)) {
            List<? extends TypeMirror> arguments = superType.getTypeArguments();
            if (!arguments.isEmpty()) {
                return arguments.getFirst();
            }
        }

        return findIdAnnotationMirror(utils, typeElement, id);
    }

}
