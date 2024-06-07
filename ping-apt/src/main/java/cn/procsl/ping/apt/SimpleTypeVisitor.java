package cn.procsl.ping.apt;

import javax.lang.model.type.*;
import java.util.function.BiFunction;

public class SimpleTypeVisitor<OUT, IN> implements TypeVisitor<OUT, IN> {


    private BiFunction<TypeMirror, IN, OUT> visit;
    private BiFunction<PrimitiveType, IN, OUT> visitPrimitive;
    private BiFunction<NullType, IN, OUT> visitNull;
    private BiFunction<ArrayType, IN, OUT> visitArray;
    private BiFunction<DeclaredType, IN, OUT> visitDeclared;
    private BiFunction<ErrorType, IN, OUT> visitError;
    private BiFunction<TypeVariable, IN, OUT> visitTypeVariable;
    private BiFunction<WildcardType, IN, OUT> visitWildcard;
    private BiFunction<ExecutableType, IN, OUT> visitExecutable;
    private BiFunction<NoType, IN, OUT> visitNoType;
    private BiFunction<TypeMirror, IN, OUT> visitUnknown;
    private BiFunction<UnionType, IN, OUT> visitUnion;
    private BiFunction<IntersectionType, IN, OUT> visitIntersection;

    @Override
    public OUT visit(TypeMirror t, IN in) {
        if (visit != null) {
            return visit.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitPrimitive(PrimitiveType t, IN in) {
        if (visitPrimitive != null) {
            return visitPrimitive.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitNull(NullType t, IN in) {
        if (visitNull != null) {
            return visitNull.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitArray(ArrayType t, IN in) {
        if (visitArray != null) {
            return visitArray.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitDeclared(DeclaredType t, IN in) {
        if (visitDeclared != null) {
            return visitDeclared.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitError(ErrorType t, IN in) {
        if (visitError != null) {
            return visitError.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitTypeVariable(TypeVariable t, IN in) {
        if (visitTypeVariable != null) {
            return visitTypeVariable.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitWildcard(WildcardType t, IN in) {
        if (visitWildcard != null) {
            return visitWildcard.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitExecutable(ExecutableType t, IN in) {
        if (visitExecutable != null) {
            return visitExecutable.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitNoType(NoType t, IN in) {
        if (visitNoType != null) {
            return visitNoType.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitUnknown(TypeMirror t, IN in) {
        if (visitUnknown != null) {
            return visitUnknown.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitUnion(UnionType t, IN in) {
        if (visitUnion != null) {
            return visitUnion.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitIntersection(IntersectionType t, IN in) {
        if (visitIntersection != null) {
            return visitIntersection.apply(t, in);
        }
        return null;
    }


    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisit(final BiFunction<TypeMirror, IN, OUT> visit) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visit = visit;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitPrimitive(final BiFunction<PrimitiveType, IN, OUT> visitPrimitive) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitPrimitive = visitPrimitive;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitNull(final BiFunction<NullType, IN, OUT> visitNull) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitNull = visitNull;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitArray(final BiFunction<ArrayType, IN, OUT> visitArray) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitArray = visitArray;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitDeclared(final BiFunction<DeclaredType, IN, OUT> visitDeclared) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitDeclared = visitDeclared;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitError(final BiFunction<ErrorType, IN, OUT> visitError) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitError = visitError;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitTypeVariable(final BiFunction<TypeVariable, IN, OUT> visitTypeVariable) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitTypeVariable = visitTypeVariable;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitWildcard(final BiFunction<WildcardType, IN, OUT> visitWildcard) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitWildcard = visitWildcard;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitExecutable(final BiFunction<ExecutableType, IN, OUT> visitExecutable) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitExecutable = visitExecutable;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitNoType(final BiFunction<NoType, IN, OUT> visitNoType) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitNoType = visitNoType;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitUnknown(final BiFunction<TypeMirror, IN, OUT> visitUnknown) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitUnknown = visitUnknown;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitUnion(final BiFunction<UnionType, IN, OUT> visitUnion) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitUnion = visitUnion;
        return tmp;
    }

    public static <OUT, IN> SimpleTypeVisitor<OUT, IN> ofVisitIntersection(final BiFunction<IntersectionType, IN, OUT> visitIntersection) {
        final SimpleTypeVisitor<OUT, IN> tmp = new SimpleTypeVisitor<>();
        tmp.visitIntersection = visitIntersection;
        return tmp;
    }


}
