package cn.procsl.ping.apt;

import lombok.NoArgsConstructor;

import javax.lang.model.element.*;
import java.util.function.BiFunction;

@NoArgsConstructor
public class SimpleElementVisitor<OUT, IN> implements ElementVisitor<OUT, IN> {

    private BiFunction<Element, IN, OUT> visit;
    private BiFunction<PackageElement, IN, OUT> visitPackage;
    private BiFunction<TypeElement, IN, OUT> visitType;
    private BiFunction<VariableElement, IN, OUT> visitVariable;
    private BiFunction<ExecutableElement, IN, OUT> visitExecutable;
    private BiFunction<TypeParameterElement, IN, OUT> visitTypeParameter;
    private BiFunction<Element, IN, OUT> visitUnknown;

    @Override
    public OUT visit(Element e, IN in) {
        if (visit != null) {
            return visit.apply(e, in);
        }
        return null;
    }

    @Override
    public OUT visitPackage(PackageElement e, IN in) {
        if (visitPackage != null) {
            return visitPackage.apply(e, in);
        }
        return null;
    }

    @Override
    public OUT visitType(TypeElement e, IN in) {
        if (visitType != null) {
            return visitType.apply(e, in);
        }
        return null;
    }

    @Override
    public OUT visitVariable(VariableElement e, IN in) {
        if (visitVariable != null) {
            return visitVariable.apply(e, in);
        }
        return null;
    }

    @Override
    public OUT visitExecutable(ExecutableElement e, IN in) {
        if (visitExecutable != null) {
            return visitExecutable.apply(e, in);
        }
        return null;
    }

    @Override
    public OUT visitTypeParameter(TypeParameterElement e, IN in) {
        if (visitTypeParameter != null) {
            return visitTypeParameter.apply(e, in);
        }
        return null;
    }

    @Override
    public OUT visitUnknown(Element e, IN in) {
        if (visitUnknown != null) {
            return visitUnknown.apply(e, in);
        }
        return null;
    }

    public static <OUT, IN> SimpleElementVisitor<OUT, IN> ofVisit(final BiFunction<Element, IN, OUT> visit) {
        SimpleElementVisitor<OUT, IN> tmp = new SimpleElementVisitor<>();
        tmp.visit = visit;
        return tmp;
    }

    public static <OUT, IN> SimpleElementVisitor<OUT, IN> ofVisitPackage(final BiFunction<PackageElement, IN, OUT> visitPackage) {
        SimpleElementVisitor<OUT, IN> tmp = new SimpleElementVisitor<>();
        tmp.visitPackage = visitPackage;
        return tmp;
    }

    public static <OUT, IN> SimpleElementVisitor<OUT, IN> ofVisitType(final BiFunction<TypeElement, IN, OUT> visitType) {
        SimpleElementVisitor<OUT, IN> tmp = new SimpleElementVisitor<>();
        tmp.visitType = visitType;
        return tmp;
    }

    public static <OUT, IN> SimpleElementVisitor<OUT, IN> ofVisitVariable(final BiFunction<VariableElement, IN, OUT> visitVariable) {
        SimpleElementVisitor<OUT, IN> tmp = new SimpleElementVisitor<>();
        tmp.visitVariable = visitVariable;
        return tmp;
    }

    public static <OUT, IN> SimpleElementVisitor<OUT, IN> ofVisitExecutable(final BiFunction<ExecutableElement, IN, OUT> visitExecutable) {
        SimpleElementVisitor<OUT, IN> tmp = new SimpleElementVisitor<>();
        tmp.visitExecutable = visitExecutable;
        return tmp;
    }

    public static <OUT, IN> SimpleElementVisitor<OUT, IN> ofVisitTypeParameter(final BiFunction<TypeParameterElement, IN, OUT> visitTypeParameter) {
        SimpleElementVisitor<OUT, IN> tmp = new SimpleElementVisitor<OUT, IN>();
        tmp.visitTypeParameter = visitTypeParameter;
        return tmp;
    }

    public static <OUT, IN> SimpleElementVisitor<OUT, IN> ofVisitUnknown(final BiFunction<Element, IN, OUT> visitUnknown) {
        SimpleElementVisitor<OUT, IN> tmp = new SimpleElementVisitor<OUT, IN>();
        tmp.visitUnknown = visitUnknown;
        return tmp;
    }


}
