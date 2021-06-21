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
import javax.ws.rs.*;
import java.lang.annotation.Annotation;
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

        ArrayList<String> template = new ArrayList<>();

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
                    template.add(param.getSimpleName().toString());
                }
            } else {
                DTOParameterModelBuilder dto = new DTOParameterModelBuilder();
                for (int i = 0; i < params.size(); i++) {
                    VariableElement param = params.get(i);
                    QueryParam queryParam = param.getAnnotation(QueryParam.class);
                    if (queryParam != null) {
                        ParameterModel model = new ParameterModel();
                        RequestParamAnnotationModel requestParam = new RequestParamAnnotationModel(param);
                        ArrayList<AnnotationModel> array = new ArrayList<>(this.directConvert(param));
                        array.add(requestParam);
                        model.setAnnotations(array);
                        model.setModifiers(Collections.singleton(Modifier.PUBLIC));
                        VariableNamingModel variable = new VariableNamingModel();
                        variable.setPackageName(param.getEnclosingElement().toString());
                        variable.setTypeName(param.getSimpleName().toString());
                        model.setType(variable);
                        parameters.add(model);
                        template.add(param.getSimpleName().toString());
                    } else {
                        dto.add(i, param);
                        template.add("dto.get" + param.getSimpleName().toString());
                    }
                }
            }
        }

        String getSimpleParam(VariableElement param) {
            {
                QueryParam queryParam = param.getAnnotation(QueryParam.class);
                if (queryParam != null) {
                    return queryParam.value();
                }
            }

            return null;
        }

        @NonNull Collection<AnnotationModel> directConvert(VariableElement param) {
            return Collections.singleton(null);
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
