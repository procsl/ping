package cn.procsl.ping.processor.restful.convert;

import cn.procsl.ping.processor.Component;

public interface Convertor<S, T extends Component> {

    S convertTo(T type);

}
