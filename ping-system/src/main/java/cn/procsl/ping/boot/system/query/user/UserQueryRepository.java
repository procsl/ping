package cn.procsl.ping.boot.system.query.user;

import cn.procsl.ping.boot.jpa.support.JpaSpecificationExecutorWithProjection;
import cn.procsl.ping.boot.system.domain.user.AccountState;
import cn.procsl.ping.boot.system.domain.user.Gender;
import cn.procsl.ping.boot.system.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserQueryRepository extends JpaSpecificationExecutorWithProjection<User, Long> {

    List<UserRecord> findAllBy();

    UserDetailsVO findById(Long id);

    Page<UserRecord> findAllByNameAndGenderAndAccountNameAndAccountStateOrderByName(String name, Gender gender, String accountName, AccountState accountState, Pageable pageable);


}
