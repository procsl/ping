package cn.procsl.ping.boot.ui.component.api;

import cn.procsl.ping.boot.ui.domain.Component;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface ComponentMapper {

    Component mapper(ComponentRecord record);

}
