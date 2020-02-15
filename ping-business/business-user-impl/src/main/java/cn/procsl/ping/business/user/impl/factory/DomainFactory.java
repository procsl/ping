package cn.procsl.ping.business.user.impl.factory;

import cn.procsl.ping.business.user.dto.UserDTO;
import cn.procsl.ping.business.user.impl.domain.UserDO;

/**
 * @author procsl
 * @date 2019/12/15
 */
public class DomainFactory {

    public UserDO create(UserDTO userDTO) {
        return new UserDO(userDTO);
    }

    public UserDO createUserById(String id) {
        return new UserDO(id);
    }
}
