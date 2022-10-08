package cn.procsl.ping.boot.admin.auth.login;

import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@ToString(exclude = {"password"})
@EqualsAndHashCode(exclude = {"password"})
public class SessionUserDetail implements UserDetails {

    @Getter
    @Hidden
    @JsonIgnore
    Long id;

    @Getter
    String nickname;
    String username;

    @Hidden
    @JsonIgnore
    String password;

    boolean enable;

    final Collection<PermissionGrantedAuthority> grant;


    public SessionUserDetail(@NonNull User user, Collection<Permission> permissions) {
        this.id = user.getId();
        this.nickname = user.getName();
        this.username = user.getAccount().getName();
        this.password = user.getAccount().getPassword();
        this.enable = user.getAccount().isEnable();
        this.grant = permissions.stream().map(PermissionGrantedAuthority::new).collect(Collectors.toList());
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grant;
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
    @Hidden
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Hidden
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Hidden
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Hidden
    @JsonIgnore
    public boolean isEnabled() {
        return enable;
    }

    @NoArgsConstructor
    final static class PermissionGrantedAuthority implements GrantedAuthority {
        String name;

        public PermissionGrantedAuthority(Permission permission) {
            this.name = permission.getOperate() + ":" + permission.getResource();
        }

        @Override
        public String getAuthority() {
            return name;
        }
    }

}
