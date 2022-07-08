package cn.procsl.ping.boot.admin.web.config.security;

import cn.procsl.ping.boot.admin.domain.user.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@ToString(exclude = {"password"})
@EqualsAndHashCode(exclude = {"password"})
public class SessionUserDetails implements UserDetails {

    @Getter

    Long id;

    @Getter
    String nickname;
    String username;
    String password;

    boolean enable;

    public SessionUserDetails(User user) {
        this.id = user.getId();
        this.nickname = user.getName();
        this.username = user.getAccount().getName();
        this.password = user.getAccount().getPassword();
        this.enable = user.getAccount().isEnable();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptySet();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enable;
    }

}
