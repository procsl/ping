package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.model.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SpringMethodModelBuilder<resolver> extends MethodModel {

    @NonNull
    final ExecutableElement executableElement;

    @NonNull
    final String fieldName;

    ParameterResolver resolver = new ParameterResolver(this);

    public SpringMethodModelBuilder(@NonNull ExecutableElement executableElement,
                                    @NonNull String fieldName,
                                    @NonNull ProcessingEnvironment processingEnv,
                                    @NonNull TypeModel typeModel) {
        this.executableElement = executableElement;
        this.fieldName = fieldName;
        this.setModifiers(Collections.singleton(Modifier.PUBLIC));
        this.setName(executableElement.getSimpleName().toString());
        this.setParent(typeModel);
        TypeMirror type = executableElement.getReturnType();
        Element element = processingEnv.getTypeUtils().asElement(type);
        this.setReturned(new NamingModel(element.getEnclosingElement().toString(), element.getSimpleName().toString()));
    }

    @Override
    public String getBody() {
        return String.format("return %s; ", resolver.getCaller());
    }

    @Override
    public List<ParameterModel> getParameters() {
        return super.getParameters();
    }

    @Override
    public Collection<AnnotationModel> getAnnotations() {
        return super.getAnnotations();
    }

    @RequiredArgsConstructor
    final static class ParameterResolver {

        @NonNull
        SpringMethodModelBuilder method;

        boolean methodSimple = isSimpleRequest();

        ArrayList<ParameterModel> parameters = new ArrayList<>(method.executableElement.getParameters().size());

        {
            List<? extends VariableElement> params = method.executableElement.getParameters();
            if (methodSimple) {
                for (VariableElement param : params) {
                    ParameterModel model = new ParameterModel();
                    model.setAnnotations(this.directConvert(param));
                    model.setModifiers(Collections.singleton(Modifier.PUBLIC));
                    VariableNamingModel variable = new VariableNamingModel();
                    variable.setPackageName(param.getEnclosingElement().toString());
                    variable.setTypeName(param.getSimpleName().toString());
                    model.setType(variable);
                    parameters.add(model);
                }
            }
        }

        Collection<AnnotationModel> directConvert(VariableElement param) {
            return null;
        }

        boolean isSimpleRequest() {
            if (method.executableElement.getAnnotation(POST.class) != null) {
                return false;
            }

            if (method.executableElement.getAnnotation(PUT.class) != null) {
                return false;
            }

            if (method.executableElement.getAnnotation(PATCH.class) != null) {
                return false;
            }
            return true;
        }


        public String getCaller() {
            return "";
        }
    }


}
