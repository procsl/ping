package cn.procsl.ping.business.user.impl.domain;

import cn.procsl.ping.business.user.dto.UserDTO;
import cn.procsl.ping.business.user.impl.entity.User;
import cn.procsl.ping.business.user.impl.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author procsl
 * @date 2019/12/15
 */
//@Slf4j
//@ToString
@Configurable(autowire = Autowire.BY_TYPE, preConstruction = true)
public class UserDO {

    private UserDTO userDTO;

    private String id;

    @Autowired
    UserRepository userRepository;

    public UserDO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public UserDO(String id) {
        this.id = id;
    }

    /**
     * 创建用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void toCreateUser() {
        if (this.userDTO == null) {
            throw new NullPointerException("用户信息不存在!");
        }

        User user = new User();
        BeanUtils.copyProperties(user, this.userDTO);
        userRepository.save(user);
    }

}
