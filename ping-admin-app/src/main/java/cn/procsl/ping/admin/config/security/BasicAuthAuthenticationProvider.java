package cn.procsl.ping.admin.config.security;

import cn.procsl.ping.boot.infra.domain.user.User;
import cn.procsl.ping.boot.infra.domain.user.UserLoadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BasicAuthAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    final UserLoadService userLoadService;

    final PasswordEncoder passwordEncoder;

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (!userDetails.isEnabled()) {
            throw new DisabledException("账户被禁用", null);
        }
        boolean matches = passwordEncoder.matches(authentication.getCredentials().toString(), userDetails.getPassword());
        if (!matches) {
            throw new BadCredentialsException("密码错误", null);
        }
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        Optional<User> optional = userLoadService.loadByAccount(username);
        User user = optional.orElseThrow(() -> new UsernameNotFoundException("账户不存在!", null));
        return new SessionUserDetails(user);
    }
}

