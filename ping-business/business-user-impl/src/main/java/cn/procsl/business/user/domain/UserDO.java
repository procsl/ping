package cn.procsl.business.user.domain;

import cn.procsl.business.user.config.UserConfig;
import cn.procsl.business.user.dto.UserDTO;
import cn.procsl.business.user.entity.User;
import cn.procsl.business.user.repository.DaoTemplate;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import javax.validation.constraints.NotNull;
import java.util.Date;

import static cn.hutool.core.util.RandomUtil.randomString;

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

    @Autowired
    @Setter
    private DaoTemplate<String, User> userDao;

    public UserDO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public UserDO(String id) {
        this.id = id;
    }

    public void save() {
        UserDTO.@NotNull Account account = userDTO.getAccount();
        User user = new User();
        this.processAccount(user, account);
        user.setPassword(account.getPassword());
        user.setCreateDate(new Date());
        user.setName(this.userDTO.getName());
        this.userDao.save(user);
        log.debug("保存账户");
    }

    public void delete() {
        log.debug("删除");
    }

    public void update() {
        log.debug("删除");
    }

    private void processAccount(User user, UserDTO.Account account) {
        switch (account.getType()) {
            case account:
                user.setAccount(account.getCredential());
                break;
            case email:
                user.setEmail(account.getCredential());
                user.setAccount(userConfig.getDefaultAccountPrefix() + randomString(17));
                break;
            case phone:
                user.setPhone(account.getCredential());
                user.setAccount(userConfig.getDefaultAccountPrefix() + randomString(17));
                break;
        }
    }
}
