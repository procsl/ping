package cn.procsl.ping.apt;

import lombok.NoArgsConstructor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.function.BiFunction;

@NoArgsConstructor
public class SimpleAnnotationValueVisitor<OUT, IN> implements AnnotationValueVisitor<OUT, IN> {

    private BiFunction<AnnotationValue, IN, OUT> visit;
    private BiFunction<Boolean, IN, OUT> visitBoolean;
    private BiFunction<Byte, IN, OUT> visitByte;
    private BiFunction<Character, IN, OUT> visitChar;
    private BiFunction<Double, IN, OUT> visitDouble;
    private BiFunction<Float, IN, OUT> visitFloat;
    private BiFunction<Integer, IN, OUT> visitInt;
    private BiFunction<Long, IN, OUT> visitLong;
    private BiFunction<Short, IN, OUT> visitShort;
    private BiFunction<String, IN, OUT> visitString;
    private BiFunction<TypeMirror, IN, OUT> visitType;
    private BiFunction<VariableElement, IN, OUT> visitEnumConstant;
    private BiFunction<AnnotationMirror, IN, OUT> visitAnnotation;

    private BiFunction<List<? extends AnnotationValue>, IN, OUT> visitArray;
    private BiFunction<AnnotationValue, IN, OUT> visitUnknown;

    @Override
    public OUT visit(AnnotationValue av, IN in) {
        if (visit != null) {
            return visit.apply(av, in);
        }
        return null;
    }

    @Override
    public OUT visitBoolean(boolean b, IN in) {
        if (visitBoolean != null) {
            return visitBoolean.apply(b, in);
        }
        return null;
    }

    @Override
    public OUT visitByte(byte b, IN in) {
        if (visitByte != null) {
            return visitByte.apply(b, in);
        }
        return null;
    }

    @Override
    public OUT visitChar(char c, IN in) {
        if (visitChar != null) {
            return visitChar.apply(c, in);
        }
        return null;
    }

    @Override
    public OUT visitDouble(double d, IN in) {
        if (visitDouble != null) {
            return visitDouble.apply(d, in);
        }
        return null;
    }

    @Override
    public OUT visitFloat(float f, IN in) {
        if (visitFloat != null) {
            return visitFloat.apply(f, in);
        }
        return null;
    }

    @Override
    public OUT visitInt(int i, IN in) {
        if (visitInt != null) {
            return visitInt.apply(i, in);
        }
        return null;
    }

    @Override
    public OUT visitLong(long i, IN in) {
        if (visitLong != null) {
            return visitLong.apply(i, in);
        }
        return null;
    }

    @Override
    public OUT visitShort(short s, IN in) {
        if (visitShort != null) {
            return visitShort.apply(s, in);
        }
        return null;
    }

    @Override
    public OUT visitString(String s, IN in) {
        if (visitString != null) {
            return visitString.apply(s, in);
        }
        return null;
    }

    @Override
    public OUT visitType(TypeMirror t, IN in) {
        if (visitType != null) {
            return visitType.apply(t, in);
        }
        return null;
    }

    @Override
    public OUT visitEnumConstant(VariableElement c, IN in) {
        if (visitEnumConstant != null) {
            return visitEnumConstant.apply(c, in);
        }
        return null;
    }

    @Override
    public OUT visitAnnotation(AnnotationMirror a, IN in) {
        if (visitAnnotation != null) {
            return visitAnnotation.apply(a, in);
        }
        return null;
    }

    @Override
    public OUT visitArray(List<? extends AnnotationValue> vals, IN in) {
        if (visitArray != null) {
            return visitArray.apply(vals, in);
        }
        return null;
    }

    @Override
    public OUT visitUnknown(AnnotationValue av, IN in) {
        if (visitUnknown != null) {
            return visitUnknown.apply(av, in);
        }
        return null;
    }


    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisit(final BiFunction<AnnotationValue, IN, OUT> visit) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visit = visit;
        return null;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitBoolean(final BiFunction<Boolean, IN, OUT> visitBoolean) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitBoolean = visitBoolean;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitByte(final BiFunction<Byte, IN, OUT> visitByte) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitByte = visitByte;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitChar(final BiFunction<Character, IN, OUT> visitChar) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitChar = visitChar;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitDouble(final BiFunction<Double, IN, OUT> visitDouble) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitDouble = visitDouble;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitFloat(final BiFunction<Float, IN, OUT> visitFloat) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitFloat = visitFloat;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitInt(final BiFunction<Integer, IN, OUT> visitInt) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitInt = visitInt;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitLong(final BiFunction<Long, IN, OUT> visitLong) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitLong = visitLong;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitShort(final BiFunction<Short, IN, OUT> visitShort) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitShort = visitShort;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitString(final BiFunction<String, IN, OUT> visitString) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitString = visitString;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitType(final BiFunction<TypeMirror, IN, OUT> visitType) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitType = visitType;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitEnumConstant(final BiFunction<VariableElement, IN, OUT> visitEnumConstant) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitEnumConstant = visitEnumConstant;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitAnnotation(final BiFunction<AnnotationMirror, IN, OUT> visitAnnotation) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitAnnotation = visitAnnotation;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitArray(final BiFunction<List<? extends AnnotationValue>, IN, OUT> visitArray) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitArray = visitArray;
        return tmp;
    }

    public static <OUT, IN> SimpleAnnotationValueVisitor<OUT, IN> ofVisitUnknown(final BiFunction<AnnotationValue, IN, OUT> visitUnknown) {
        SimpleAnnotationValueVisitor<OUT, IN> tmp = new SimpleAnnotationValueVisitor<>();
        tmp.visitUnknown = visitUnknown;
        return tmp;
    }


}
