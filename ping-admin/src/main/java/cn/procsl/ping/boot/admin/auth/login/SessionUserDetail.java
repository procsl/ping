package cn.procsl.ping.boot.admin.auth.login;

import cn.procsl.ping.boot.admin.auth.access.GrantedAuthorityFactory;
import cn.procsl.ping.boot.admin.domain.rbac.Permission;
import cn.procsl.ping.boot.admin.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@ToString(exclude = {"password"})
@EqualsAndHashCode(exclude = {"password", "authorities"})
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

    @Getter
    Collection<GrantedAuthority> authorities;

    public SessionUserDetail(@NonNull User user, @NonNull Collection<Permission> permissions) {
        this.id = user.getId();
        this.nickname = user.getName();
        this.username = user.getAccount().getName();
        this.password = user.getAccount().getPassword();
        this.enable = user.getAccount().isEnable();
        this.authorities = permissions.stream()
                                      .map(GrantedAuthorityFactory::create)
                                      .filter(Objects::nonNull)
                                      .collect(Collectors.toList());

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

}
