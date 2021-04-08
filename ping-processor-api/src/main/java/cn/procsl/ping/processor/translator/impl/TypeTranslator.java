package cn.procsl.ping.processor.translator.impl;

import cn.procsl.ping.processor.translator.Translator;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public interface TypeTranslator extends Translator<TypeSpec> {

    void addAnnotation(Translator<AnnotationSpec> translator);

    void addField(Translator<FieldSpec> translator);

    void addMethod(Translator<MethodSpec> translator);

}
