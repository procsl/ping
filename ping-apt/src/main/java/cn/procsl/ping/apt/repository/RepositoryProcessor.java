package cn.procsl.ping.apt.neo.repository;

import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public interface RepositoryProcessor {
    void processor(
        ProcessingEnvironment env,
        RoundEnvironment roundEnv,
        TypeElement entity,
        AnnotationMirror createRepositoryMirror,
        TypeSpec.Builder builder,
        DeclaredType repositoryValue,
        TypeElement repositoryElement,
        TypeMirror idMirror
    );


}
