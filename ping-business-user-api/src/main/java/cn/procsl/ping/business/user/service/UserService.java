package cn.procsl.ping.business.user.service;

import cn.procsl.ping.business.user.dto.UserDTO;
import lombok.NonNull;

/**
 * @author procsl
 * @date 2020/03/22
 */
public interface UserService {

    /**
     * 创建用户
     *
     * @param user
     */
    void create(@NonNull UserDTO user);

}
