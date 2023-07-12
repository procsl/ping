package cn.procsl.ping.boot.system.api.user;

import cn.procsl.ping.boot.system.domain.user.Authenticate;
import cn.procsl.ping.boot.system.domain.user.User;
import org.mapstruct.Mapper;


@Mapper
public interface UserMapper {

    AuthenticateVO mapper(Authenticate authenticate);

    UserDetailVO mapper(User user);

}
