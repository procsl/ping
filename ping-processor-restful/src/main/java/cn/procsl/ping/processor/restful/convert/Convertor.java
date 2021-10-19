package cn.procsl.ping.processor.restful.convert;

import cn.procsl.ping.processor.Model;

public interface Convertor<S, T extends Model> {

    S convertTo(T type);

}
