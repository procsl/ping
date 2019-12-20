package cn.procsl.business.user.factory;

import cn.procsl.business.user.domain.UserDO;
import cn.procsl.business.user.dto.UserDTO;

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
