package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.api.user.UserDetailVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQueryRepository {

    List<UserRecord> findAllBy();

    UserDetailVO findById(Long id);


}
