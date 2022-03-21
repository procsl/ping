package cn.procsl.ping.boot.user.account;

import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;

@Indexed
@RequiredArgsConstructor
@Service
public class AccountService {

    final JpaRepository<AccountEntity, Long> jpaRepository;

    /**
     * 注册账户
     *
     * @param account 账户信息
     * @return
     */
    public Long create(@NotNull Account account) {

        if (this.jpaRepository.exists(Example.of(new AccountEntity(account.getName())))) {
            throw new BusinessException("账户已被注册");
        }

        AccountEntity entity = new AccountEntity();
        BeanUtils.copyProperties(account, entity);
        entity.enabled();
        return jpaRepository.save(entity).getId();
    }

}
