package cn.procsl.ping.boot.infra.adapter.user;

import cn.procsl.ping.boot.infra.domain.user.AuthenticateService;
import cn.procsl.ping.boot.infra.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

@Indexed
@Service
@RequiredArgsConstructor
class AuthenticateServiceAdapter implements AuthenticateService {

    final AccountService accountService;

    @Override
    public Long create(String account, String password) {
        return accountService.create(account, password);
    }

    @Override
    public Long authenticate(String account, String password) {
        return accountService.authenticate(account, password);
    }


}
