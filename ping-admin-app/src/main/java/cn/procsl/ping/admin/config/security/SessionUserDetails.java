package cn.procsl.ping.admin.config.security;

import cn.procsl.ping.boot.infra.domain.rbac.Role;
import cn.procsl.ping.boot.infra.domain.user.User;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Data
public class SessionUserDetails implements UserDetails {

    Long id;

    @Getter
    String nickname;
    String username;
    String password;

    boolean enable;

    @Getter
    Set<Long> roles;

    @Getter
    Set<String> permissions;

    public SessionUserDetails(User user) {
        this.id = user.getId();
        this.nickname = user.getName();
        this.username = user.getAccount().getName();
        this.password = user.getAccount().getPassword();
        this.enable = user.getAccount().isEnable();
        this.roles = Collections.emptySet();
        this.permissions = Collections.emptySet();
    }

    public void setRoles(Collection<Role> roles) {
        if (roles != null) {
            this.roles = Collections.emptySet();
            this.permissions = Collections.emptySet();
        }
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
