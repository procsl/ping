package cn.procsl.ping.boot.user.account;

import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends org.springframework.data.repository.Repository<Account, Long> {


    boolean existsAccountByName(String name);

}
