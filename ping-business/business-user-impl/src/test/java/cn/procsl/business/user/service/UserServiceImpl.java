package cn.procsl.business.user.service;

import cn.procsl.business.user.dto.UserDTO;
import cn.procsl.business.user.entity.User;
import cn.procsl.business.user.repository.HibernateDaoTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author procsl
 * @date 2019/12/17
 */
@Service
public class UserServiceImpl implements IUserService {

//    @Autowired
//    HibernateDaoTemplate<String, User> hibernateDaoTemplate;

    @Autowired
    HibernateTemplate template;

    @Override
    @Transactional
    public void create() {
        User user = new User();
        user.setGender(UserDTO.Gender.男)
                .setVersion(1L)
                .setAccount("12312312")
                .setEmail("program_chen@foxmail.com")
                .setPhone("hhhhe")
                .setName("朝闻道")
                .setCreateDate(new Date());
        template.save(user);
//        hibernateDaoTemplate.save(user);
    }
}
