package cn.procsl.ping.boot.infra.service;

import cn.procsl.ping.boot.infra.domain.user.User;
import cn.procsl.ping.exception.BusinessException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public interface UserService {
    Long register(@NotBlank @Size(min = 5, max = 20) String account, @NotBlank @Size(min = 5, max = 20) String password) throws BusinessException;

    User login(@NotBlank String account, @NotBlank String password) throws BusinessException;

}
