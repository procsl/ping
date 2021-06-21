package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.MethodModel;
import cn.procsl.ping.processor.model.NamingModel;
import cn.procsl.ping.processor.model.ParameterModel;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.Collection;
import java.util.HashMap;

public class DTOParameterModelBuilder extends ParameterModel {

    HashMap<Integer, VariableElement> hashMap = new HashMap();

    public void add(int i, VariableElement param) {

    }

    @Override
    public NamingModel getType() {
        return new NamingModel();
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public MethodModel getParent() {
        return super.getParent();
    }

    @Override
    public Collection<Modifier> getModifiers() {
        return super.getModifiers();
    }

    @Override
    public Collection<AnnotationModel> getAnnotations() {
        return super.getAnnotations();
    }
}
