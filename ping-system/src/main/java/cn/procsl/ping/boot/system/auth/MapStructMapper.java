package cn.procsl.ping.boot.system.auth;

import cn.procsl.ping.boot.system.domain.auth.Authentication;
import org.mapstruct.Mapper;


@Mapper
public interface MapStructMapper {

    SessionUserDetail mapper(Authentication authentication);


}
