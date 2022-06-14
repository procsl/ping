package cn.procsl.ping.boot.infra.domain.user;

import cn.procsl.ping.boot.domain.service.PasswordEncoderService;
import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Indexed
@Service
@RequiredArgsConstructor
public class UserRegisterService {

    final RoleSettingService roleSettingService;

    final JpaRepository<User, Long> jpaRepository;

    final JpaRepository<Account, Long> accountRepository;

    final JpaSpecificationExecutor<User> userJpaSpecificationExecutor;

    final PasswordEncoderService passwordEncoderService;

    /**
     * 用户注册, 用户名称默认为账户名称
     *
     * @param account  账户名称
     * @param password 用户密码
     * @throws BusinessException 如果注册失败
     */
    @Validated
    public Long register(String account, String password) throws BusinessException {
        password = passwordEncoderService.encode(password);
        User user = User.creator(account, account, password);
        this.accountRepository.save(user.getAccount());
        this.jpaRepository.save(user);
        this.roleSettingService.grantDefaultRoles(user.getId());
        return user.getId();
    }


}
