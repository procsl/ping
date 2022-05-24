package cn.procsl.ping.boot.infra.service.impl;

import cn.procsl.ping.boot.domain.valid.UniqueField;
import cn.procsl.ping.boot.infra.domain.account.Account;
import cn.procsl.ping.boot.infra.service.AccountService;
import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Indexed
@Service
@Validated
@RequiredArgsConstructor
class AccountServiceImpl implements AccountService {

    final JpaRepository<Account, Long> jpaRepository;

    final JpaSpecificationExecutor<Account> jpaSpecificationExecutor;

    /**
     * 注册账户
     *
     * @param name     账户名称
     * @param password 密码
     * @return 返回账户ID
     * @throws BusinessException 如果创建账户失败
     */
    @Transactional
    @Override
    public Long create(@NotBlank @UniqueField(entity = Account.class, fieldName = "name", message = "账户已被注册") String name, @NotBlank String password) throws BusinessException {
        return jpaRepository.save(Account.create(name, password)).getId();
    }


    /**
     * @param account  账户名称
     * @param password 账户密码
     * @return 如果登录成功返回账户ID
     * @throws BusinessException 如果登录失败
     */
    @Override
    @Transactional(readOnly = true)
    public Long authenticate(@NotBlank String account, @NotBlank String password) throws BusinessException {
        Optional<Account> entity = this.jpaSpecificationExecutor.findOne((root, query, cb) -> cb.equal(root.get("account"), account));
        entity.ifPresent(item -> item.authenticate(password));
        return entity.orElseThrow(() -> new BusinessException("账户不存在")).getId();
    }

    @Override
    @Transactional
    public void disable(@NotNull Long id) throws BusinessException {
        Account account = this.jpaRepository.getById(id);
        account.disabled();
        this.jpaRepository.save(account);
    }

    @Override
    @Transactional
    public void enable(@NotNull Long id) throws BusinessException {
        Account account = this.jpaRepository.getById(id);
        account.enabled();
        this.jpaRepository.save(account);
    }

}
