package cn.procsl.ping.business.user.service;

import cn.procsl.ping.business.user.dto.UserDTO;
import lombok.NonNull;

import javax.validation.Valid;

/**
 * 用户服务
 * 包含用户相关的服务接口
 *
 * @author procsl
 * @date 2019/12/11
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param user 用户信息
     */
    void create(@NonNull @Valid UserDTO user);

}
