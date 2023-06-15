package cn.procsl.ping.boot.system.query;

import org.springframework.stereotype.Repository;

@Repository
public interface UserQueryRepository {

    UserDetailsVO find();

}
