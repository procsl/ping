package cn.procsl.ping.apt.repository;

import cn.procsl.ping.apt.AptUtils;
import cn.procsl.ping.apt.SimpleAnnotationValueVisitor;
import cn.procsl.ping.apt.SimpleElementVisitor;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

import static cn.procsl.ping.apt.SimpleAnnotationValueVisitor.ofVisitArray;
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
    final Map<String, RepositoryProcessor> processor = new HashMap<>();

    final RepositoryProcessor empty = (a, b, c, d, e, f, g, h) ->
        a.getMessager().printMessage(ERROR, "无法处理的类型: " + g.toString(), c);

    {
        var a = new JpaIdAndEntityRepositoryProcessor("org.springframework.data.jpa.repository.JpaRepository");
        var b = new JpaIdAndEntityRepositoryProcessor("org.springframework.data.jpa.repository.ListCrudRepository");
        var c = new JpaIdAndEntityRepositoryProcessor("org.springframework.data.jpa.repository.ListPagingAndSortingRepository");
        var d = new JpaIdAndEntityRepositoryProcessor("org.springframework.data.jpa.repository.CrudRepository");
        var e = new JpaIdAndEntityRepositoryProcessor("org.springframework.data.jpa.repository.Repository");
        var f = new EntityRepositoryProcessor("org.springframework.data.jpa.repository.JpaSpecificationExecutor");
        processor.put(a.getName(), a);
        processor.put(b.getName(), b);
        processor.put(c.getName(), c);
        processor.put(d.getName(), d);
        processor.put(e.getName(), e);
        processor.put(f.getName(), f);
    }


    @Override
    public void build(ProcessingEnvironment env, RoundEnvironment roundEnv, TypeElement anno, Element target) {

        TypeElement entity = target.accept(visitor, null);
        if (entity == null) {
            return;
        }

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

        var builder = TypeSpec.interfaceBuilder(className).addModifiers(Modifier.PUBLIC);

        Supplier<Map<String, CodeBlock>> sup = () -> {
            var value = CodeBlock.of("$S", this.getClass().getName());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC+8"));
            String formattedDate = sdf.format(new Date());
            var date = CodeBlock.of("$S", formattedDate);
            return Map.of("value", value, "date", date);
        };

        this.addRequiredAnnotation(builder, "javax.annotation.processing.Generated", sup);
        this.addRequiredAnnotation(builder, "org.springframework.stereotype.Repository", null);
        this.addAnnotationIfExistsOnClassPath(builder, "org.springframework.stereotype.Indexed", null);

        AnnotationValue value = AptUtils.findAnnotationValueOrDefaultValue(env.getElementUtils(), annotationMirror, "repositories");

        do {

            if (value == null) {
                break;
            }

            var arrays = value.accept(ofVisitArray((e, o) -> e), null);
            if (arrays == null) {
                break;
            }
            var visitType = SimpleAnnotationValueVisitor.ofVisitType((e, o) -> e);
            var declared = ofVisitDeclared((e, o) -> e);
            for (AnnotationValue item : arrays) {
                TypeMirror inf = item.accept(visitType, null);
                if (inf == null) {
                    continue;
                }
                DeclaredType repo = inf.accept(declared, null);
                if (repo == null) {
                    continue;
                }
                TypeElement repositoryType = repo.asElement().accept(ofVisitType((e, o) -> e), null);
                if (repositoryType == null) {
                    continue;
                }

                try {
                    String key = repositoryType.getQualifiedName().toString();
                    TypeMirror idMirror = this.findIdAnnotationMirror(env.getElementUtils(), entity, id);
                    this.processor.getOrDefault(key, empty)
                        .processor(env, roundEnv, entity, annotationMirror, builder, repo, repositoryType, idMirror);
                } catch (RuntimeException ex) {
                    env.getMessager().printMessage(ERROR, ex.getMessage(), entity);
                }
            }

        } while (false);

        String packageName = this.createRepositoryPackageName(env.getElementUtils(), entity, annotationMirror);
        AptUtils.writer(env.getMessager(), env.getFiler(), packageName, builder.build());
    }


    public void addRequiredAnnotation(TypeSpec.Builder builder, String clazz, Supplier<Map<String, CodeBlock>> getter) {

        AnnotationSpec.Builder annotation = AnnotationSpec.builder(ClassName.bestGuess(clazz));
        Map<String, CodeBlock> arguments = (getter != null) ? getter.get() : Collections.emptyMap();
        if (arguments != null) {
            arguments.forEach(annotation::addMember);
        }
        builder.addAnnotation(annotation.build());
    }

    public void addAnnotationIfExistsOnClassPath(TypeSpec.Builder builder, String clazz, Supplier<Map<String, CodeBlock>> arguments) {
        if (!AptUtils.isAvailable(clazz)) {
            this.addRequiredAnnotation(builder, clazz, arguments);
        }
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(repository_creator_class);
    }

    private String createRepositoryClassName(Elements utils, TypeElement entity, AnnotationMirror repo) {

        AnnotationValue annotation = AptUtils.findAnnotationValueOrDefaultValue(utils, repo, "repositoryName");
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
        AnnotationValue annotation = AptUtils.findAnnotationValueOrDefaultValue(utils, repo, "packageName");
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
