package cn.procsl.ping.boot.domain.business.dictionary.model;

import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

import static java.lang.Integer.parseInt;

@RequiredArgsConstructor
public enum Type implements Serializable {

    blank("空") {
        @Override
        public String format(java.lang.String input) {
            return "";
        }
    },

    integer("数字") {
        @Override
        public Integer format(String input) {
            return parseInt(input);
        }
    },

    string("字符串") {
        @Override
        public String format(String input) {
            return input;
        }
    },
    ;

    final String desc;

    public <T> T format(String input) {
        throw new UnsupportedOperationException("未重写的格式化操作");
    }
}
