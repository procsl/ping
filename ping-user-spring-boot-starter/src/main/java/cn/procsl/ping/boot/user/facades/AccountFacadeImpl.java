package cn.procsl.ping.boot.user.facades;

import cn.procsl.ping.boot.account.AccountService;
import cn.procsl.ping.boot.user.AccountFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

@Indexed
@Service
@RequiredArgsConstructor
public class AccountFacadeImpl implements AccountFacade {

    final AccountService accountService;

    @Override
    public Long create(String account, String password) {
        return accountService.create(account, password);
    }


}
