package cn.procsl.ping.processor.builder;

import cn.procsl.ping.processor.model.AnnotationModel;
import cn.procsl.ping.processor.model.NamingModel;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.ws.rs.QueryParam;
import java.util.HashMap;


public class RequestParamAnnotationModel extends AnnotationModel {
    private final HashMap<String, String> paramMap;
    private final VariableElement param;

    public RequestParamAnnotationModel(VariableElement param) {
        super(new NamingModel("org.springframework.web.bind.annotation", "RequestParam"));
        this.paramMap = new HashMap<>();
        this.setValueMap(paramMap);
        this.param = param;

        if (hasQueryParam()) {

        }
    }


    boolean hasQueryParam() {
        return param.getAnnotation(QueryParam.class) != null;
    }

}
