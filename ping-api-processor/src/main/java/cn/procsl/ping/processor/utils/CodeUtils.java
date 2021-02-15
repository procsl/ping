package cn.procsl.ping.processor.utils;

//import cn.procsl.ping.processor.model.AnnotationCodeBlackModel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CodeUtils {

    public static String convertToTemplate(Object[] args, String item) {
        if (args == null || args.length == 0) {
            return "";
        }
        return Arrays.stream(args).map(i -> item).collect(Collectors.joining(","));
    }

    public static String convertToTemplate(List<?> args, String item) {
        if (args == null || args.isEmpty()) {
            return "";
        }

        Object[] tmp = args.toArray();
        return convertToTemplate(tmp, item);
    }

    public static String convertToTemplate(int times, String item) {
        if (times <= 0) {
            return "";
        }

        Object[] tmp = new Object[times];
        return convertToTemplate(tmp, item);
    }

//
//    public static AnnotationSpec convertTo(AnnotationModel annotation) {
//        ClassName annotationType = ClassName.get(annotation.getPackageName(), annotation.getName());
//        AnnotationSpec.Builder annotationBuilder = AnnotationSpec.builder(annotationType);
////        for (AnnotationCodeBlackModel annotationCodeBlackModel : annotation.getCodeBlack()) {
////            List<String> args = annotationCodeBlackModel.getArguments();
////            annotationBuilder.addMember(annotationCodeBlackModel.getName(), annotationCodeBlackModel.sourceTemplate(), args.toArray());
////        }
//        return annotationBuilder.build();
//    }


}
