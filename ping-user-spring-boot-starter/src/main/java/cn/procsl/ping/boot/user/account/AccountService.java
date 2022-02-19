package cn.procsl.ping.boot.user.account;

import cn.procsl.ping.business.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

@Indexed
@RequiredArgsConstructor
@Service
public class AccountService {

    final JpaRepository<Account, Long> jpaRepository;

    /**
     * 检查是否可添加账户
     *
     * @param userId 用户ID
     * @param name   账户名
     * @throws BusinessException 如果无法添加账户
     */
    public void canRegistered(@NotNull Long userId, @NotNull @Size(min = 5, max = 15) String name) throws BusinessException {

        if (this.jpaRepository.exists(Example.of(new Account(name)))) {
            throw new BusinessException(401, "U001", "账户已被注册");
        }
    }


    /**
     * 注册账户
     *
     * @param userId   绑定的userId
     * @param name     账户名称
     * @param password 账户密码
     */
    public void registered(@NotNull Long userId, @NotNull @Size(min = 5, max = 15) String name, @NotNull String password) {
        this.canRegistered(userId, name);
        Account account = new Account();
        account.setUserId(userId);
        account.setName(name);
        account.setPassword(password);
        account.enabled();
        jpaRepository.save(account);
    }

    /**
     * 账户登录
     *
     * @param name     账户名称
     * @param password 密码
     * @return 登录成功返回用户ID
     * @throws BusinessException 如果登录失败, 则抛出异常
     */
    public Long loginByPassword(@NotNull String name, @NotNull String password) throws BusinessException {

        Optional<Account> account = this.jpaRepository.findOne(Example.of(new Account(name)));
        account.orElseThrow(() -> new BusinessException(403, "U002", "账户或密码错误"));
        account.ifPresent(item -> {
            if (item.isDisabled()) {
                throw new BusinessException(403, "U002", "账户已被禁用");
            }

            if (item.checkPassword(password)) {
                throw new BusinessException(403, "U002", "账户或密码错误");
            }
        });
        return account.get().getUserId();
    }

}
