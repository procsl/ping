package cn.procsl.ping.boot.account;

import cn.procsl.ping.boot.domain.valid.UniqueField;
import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Indexed
@Service
@RequiredArgsConstructor
@Validated
public class AccountService {

    final JpaRepository<Account, Long> jpaRepository;

    /**
     * 注册账户
     *
     * @param name     账户名称
     * @param password 密码
     * @return 返回账户ID
     * @throws BusinessException 如果创建账户失败
     */
    @Transactional
    public Long create(
        @NotBlank @UniqueField(entity = Account.class, fieldName = "name", message = "账户已被注册") String name,
        @NotBlank String password
    ) throws BusinessException {
        return jpaRepository.save(new Account(name, password)).getId();
    }


    /**
     * @param name     账户名称
     * @param password 账户密码
     * @return 如果登录成功返回账户ID
     * @throws BusinessException 如果登录失败
     */
    @Transactional(readOnly = true)
    public Long login(@NotBlank String name, @NotBlank String password) throws BusinessException {
        Optional<Account> account = this.jpaRepository.findOne(Example.of(new Account(name)));
        account.ifPresent(item -> {
            if (!item.isEnable()) {
                throw new BusinessException("账户已被禁用");
            }

            if (!item.checkPassword(password)) {
                throw new BusinessException("密码错误");
            }
        });
        return account.orElseThrow(() -> new BusinessException("账户不存在")).getId();
    }

    @Transactional
    public void disable(@NotNull Long id) throws BusinessException {
        Account account = this.jpaRepository.getById(id);
        account.disabled();
        this.jpaRepository.save(account);
    }

    @Transactional
    public void enable(@NotNull Long id) throws BusinessException {
        Account account = this.jpaRepository.getById(id);
        account.enabled();
        this.jpaRepository.save(account);
    }

}
