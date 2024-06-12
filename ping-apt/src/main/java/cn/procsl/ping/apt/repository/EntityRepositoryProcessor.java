package cn.procsl.ping.apt.neo.repository;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

@Getter
@RequiredArgsConstructor
final class EntityRepositoryProcessor implements RepositoryProcessor {

    final private String name;

    @Override
    public void processor(ProcessingEnvironment env, RoundEnvironment roundEnv, TypeElement entity,
                          AnnotationMirror createRepositoryMirror, TypeSpec.Builder builder,
                          DeclaredType repositoryValue, TypeElement repositoryElement, TypeMirror idMirror) {
        TypeName params = ParameterizedTypeName.get(ClassName.get(repositoryElement), TypeName.get(entity.asType()));
        builder.addSuperinterface(params);
    }

}
