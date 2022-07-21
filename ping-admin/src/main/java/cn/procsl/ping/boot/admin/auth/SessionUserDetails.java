package cn.procsl.ping.boot.admin.auth;

import cn.procsl.ping.boot.admin.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Builder;
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

    public SessionUserDetails(User user) {
        this.id = user.getId();
        this.nickname = user.getName();
        this.username = user.getAccount().getName();
        this.password = user.getAccount().getPassword();
        this.enable = user.getAccount().isEnable();
    }

    @Builder
    SessionUserDetails(Long id, String nickname, String username, String password, boolean enable) {
        this.id = id;
        this.nickname = nickname;
        this.username = username;
        this.password = password;
        this.enable = enable;
    }

    public static SessionUserDetails anno(String name) {
        return SessionUserDetails.builder().username(name).nickname(name).build();
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
