package cn.procsl.ping.business.user.impl.service;

import cn.procsl.ping.business.user.dto.UserDTO;
import cn.procsl.ping.business.user.impl.entity.User;
import cn.procsl.ping.business.user.impl.repository.UserRepository;
import cn.procsl.ping.business.user.service.UserService;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * @author procsl
 * @date 2020/03/21
 */
@Service
@Validated
@Slf4j
public class UserServiceImpl implements UserService {

    @Setter
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(@NonNull UserDTO user) {
        User userEntity = new User();
        BeanUtils.copyProperties(user,userEntity);
        userRepository.save(userEntity);
    }
}
