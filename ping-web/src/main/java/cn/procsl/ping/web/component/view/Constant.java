package cn.procsl.ping.web.component.view;

import lombok.Getter;

import java.util.Map;

/**
 * @author procsl
 * @date 2020/01/07
 */
public enum Constant {
    RETURN_VALUE("__RETURN__VALUE__"), ERROR_VALUE("__ERROR_VALUE__");

    @Getter
    private String value;

    Constant(String value) {
        this.value = value;
    }

    public static Object filter(Map<String, Object> model) {
        if (model.containsKey(Constant.RETURN_VALUE.value)) {
            return model.get(Constant.RETURN_VALUE.value);
        }

        if (model.containsKey(Constant.ERROR_VALUE.value)) {
            return model.get(Constant.ERROR_VALUE.value);
        }
        return null;
    }
}
