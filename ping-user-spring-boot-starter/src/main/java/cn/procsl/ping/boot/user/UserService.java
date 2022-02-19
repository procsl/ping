package cn.procsl.ping.boot.user;

import cn.procsl.ping.boot.user.account.AccountService;
import cn.procsl.ping.boot.user.rbac.AccessControlService;
import cn.procsl.ping.business.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;

@Indexed
@Service
@RequiredArgsConstructor
public class UserService {

    final AccessControlService accessControlService;

    final AccountService accountService;

    final JpaRepository<User, Long> jpaRepository;

    public Long registered(@NotNull String userName, @NotNull String gender, @NotNull String accountName, @NotNull String password, @NotNull Collection<String> roleNames) throws BusinessException {
        User user = new User();
        user.setName(userName);
        user.setGender(gender);
        this.jpaRepository.save(user);
        this.accountService.registered(user.getId(), accountName, password);
        this.accessControlService.grant(user.getId(), roleNames);
        return user.getId();
    }

    public void quickRegistered(String userName, String password) {
        this.registered(userName, "未知", userName, password, Collections.emptyList());
    }

}
