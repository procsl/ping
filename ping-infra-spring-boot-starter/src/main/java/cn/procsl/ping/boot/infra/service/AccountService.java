package cn.procsl.ping.boot.infra.service;

import cn.procsl.ping.boot.domain.valid.UniqueField;
import cn.procsl.ping.boot.infra.domain.account.Account;
import cn.procsl.ping.exception.BusinessException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface AccountService {
    Long create(@NotBlank @UniqueField(entity = Account.class, fieldName = "name", message = "账户已被注册") String name, @NotBlank String password) throws BusinessException;

    Long authenticate(@NotBlank String account, @NotBlank String password) throws BusinessException;

    void disable(@NotNull Long id) throws BusinessException;

    void enable(@NotNull Long id) throws BusinessException;
}
