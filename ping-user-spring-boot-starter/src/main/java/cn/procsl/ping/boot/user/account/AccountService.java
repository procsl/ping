package cn.procsl.ping.boot.user.account;

import cn.procsl.ping.business.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class AccountService {

    @Inject
    JpaRepository<Account, Long> jpaRepository;

    @Inject
    AccountRepository accountRepository;

    public void checkRegisterAccount(@NotNull Long userId, @NotNull String name, @NotNull String password) throws BusinessException {

        if (accountRepository.existsAccountByName(name)) {
            throw new BusinessException(401, "U001", "账户已被注册");
        }

    }

}
