package cn.procsl.ping.processor.v3;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

public interface TargetVisitor<R, P> {

    default R visitType(TypeElement e, Descriptor descriptor, P p) {
        return null;
    }

    default R visitVariable(VariableElement e, Descriptor descriptor, P p) {
        return null;
    }

    default R visitExecutable(ExecutableElement e, Descriptor descriptor, P p) {
        return null;
    }


}
