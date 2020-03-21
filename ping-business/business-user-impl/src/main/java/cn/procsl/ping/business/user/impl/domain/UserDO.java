package cn.procsl.ping.business.user.impl.domain;

import cn.procsl.ping.business.user.dto.UserDTO;
import cn.procsl.ping.business.user.impl.config.UserConfig;
import cn.procsl.ping.business.user.impl.entity.User;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author procsl
 * @date 2019/12/15
 */
@Slf4j
@ToString
@Configurable(autowire = Autowire.BY_TYPE, preConstruction = true)
public class UserDO {

    private UserDTO userDTO;

    private String id;

    @Autowired
    @Setter
    private UserConfig userConfig;

    public UserDO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public UserDO(String id) {
        this.id = id;
    }

    public void save() {

    }

    public void delete() {
        log.debug("删除");
    }

    public void update() {
        log.debug("删除");
    }

    private void processAccount(User user, UserDTO.Account account) {

    }
}
