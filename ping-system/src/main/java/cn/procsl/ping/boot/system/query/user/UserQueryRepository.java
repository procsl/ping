package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.system.domain.user.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserQueryRepository extends org.springframework.data.repository.Repository<User, Long> {

    @Transactional(readOnly = true)
    List<UserRecord> findAll();

}
