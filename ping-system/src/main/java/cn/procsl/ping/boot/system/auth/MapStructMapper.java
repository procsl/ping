package cn.procsl.ping.boot.system.auth;

import cn.procsl.ping.boot.system.domain.session.Session;
import org.mapstruct.Mapper;


@Mapper
public interface MapStructMapper {

    SessionUserDetail mapper(Session session);


}