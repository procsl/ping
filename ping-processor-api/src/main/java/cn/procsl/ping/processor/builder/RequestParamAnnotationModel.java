package cn.procsl.ping.processor.builder;


import javax.lang.model.element.VariableElement;


public class RequestParamAnnotationModel {
//    private final HashMap<String, String> paramMap;
//    private final VariableElement param;

    public RequestParamAnnotationModel(VariableElement param) {
//        super(new NamingModel("org.springframework.web.bind.annotation", "RequestParam"));
//        this.paramMap = new HashMap<>();
//        this.setValueMap(paramMap);
//        this.param = param;
//
//        if (hasQueryParam()) {
//
//        }
    }


    boolean hasQueryParam() {
//        return param.getAnnotation(QueryParam.class) != null;
        return false;
    }

}
