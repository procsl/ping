package cn.procsl.ping.boot.user.account;

import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Indexed
@RequiredArgsConstructor
@Service
public class AccountService {

    final JpaRepository<AccountEntity, Long> jpaRepository;

    /**
     * 注册账户
     *
     * @param account 账户信息
     */
    public void registered(@NotNull Account account) {

        if (this.jpaRepository.exists(Example.of(new AccountEntity(account.getName())))) {
            throw new BusinessException("账户已被注册");
        }

        AccountEntity entity = new AccountEntity();
        BeanUtils.copyProperties(account, entity);
        entity.enabled();
        jpaRepository.save(entity);
    }

    /**
     * 账户登录
     *
     * @param account  账户名称
     * @param password 密码
     * @return 登录成功返回用户ID
     * @throws BusinessException 如果登录失败, 则抛出异常
     */
    public Long loginByPassword(@NotNull String account, @NotNull String password) throws BusinessException {

        Optional<AccountEntity> entity = this.jpaRepository.findOne(Example.of(new AccountEntity(account)));
        entity.orElseThrow(() -> new BusinessException("账户或密码错误"));
        entity.ifPresent(item -> {
            if (item.isDisabled()) {
                throw new BusinessException("账户已被禁用");
            }

            if (item.checkPassword(password)) {
                throw new BusinessException("账户或密码错误");
            }
        });
        return entity.get().getUserId();
    }

}
