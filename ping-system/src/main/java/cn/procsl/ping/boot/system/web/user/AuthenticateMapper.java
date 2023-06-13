package cn.procsl.ping.boot.system.web.user;

import cn.procsl.ping.boot.system.domain.user.Authenticate;
import org.mapstruct.Mapper;


@Mapper
public interface AuthenticateMapper {

    AuthenticateVO mapper(Authenticate authenticate);


}
