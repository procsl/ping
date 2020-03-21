package cn.procsl.ping.business.user.impl.repository;

import cn.procsl.ping.business.user.impl.entity.User;
import org.springframework.data.repository.CrudRepository;

/**
 * @author procsl
 * @date 2020/03/21
 */
public interface UserRepository extends CrudRepository<User, String> {
}
