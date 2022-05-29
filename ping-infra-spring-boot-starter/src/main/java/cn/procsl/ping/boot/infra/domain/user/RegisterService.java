package cn.procsl.ping.boot.infra.domain.user;

import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Indexed
@Service
@RequiredArgsConstructor
public class RegisterService {

    final AuthenticateService authenticateService;

    final AuthorizedService authorizedService;

    final JpaRepository<User, Long> jpaRepository;

    /**
     * 用户注册, 用户名称默认为账户名称
     *
     * @param account  账户名称
     * @param password 用户密码
     * @throws BusinessException 如果注册失败
     */
    public Long register(@NotBlank @Size(min = 5, max = 20) String account, @NotBlank @Size(min = 5, max = 20) String password) throws BusinessException {
        Long accountId = authenticateService.create(account, password);
        User user = this.jpaRepository.save(new User(account, accountId));
        authorizedService.grantDefaultRoles(user.getId());
        return user.getId();
    }

}
