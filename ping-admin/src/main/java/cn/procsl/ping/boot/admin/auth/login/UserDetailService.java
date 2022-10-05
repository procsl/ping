package cn.procsl.ping.boot.admin.auth.login;

import cn.procsl.ping.boot.admin.domain.user.User;
import cn.procsl.ping.boot.admin.domain.user.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Indexed
@RequiredArgsConstructor
@Service(value = "securityUserDetailService")
public class UserDetailService implements UserDetailsService {

    final JpaSpecificationExecutor<User> userJpaSpecificationExecutor;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optional = userJpaSpecificationExecutor.findOne(new UserSpecification(username));
        User user = optional.orElseThrow(() -> new UsernameNotFoundException("账户不存在!", null));
        return new SessionUserDetail(user);
    }


}
