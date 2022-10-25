package cn.procsl.ping.boot.admin.auth.login;

import cn.procsl.ping.boot.admin.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@ToString(exclude = {"password"})
@EqualsAndHashCode(exclude = {"password"})
public class SessionUserDetail {

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


    public SessionUserDetail(@NonNull User user) {
        this.id = user.getId();
        this.nickname = user.getName();
        this.username = user.getAccount().getName();
        this.password = user.getAccount().getPassword();
        this.enable = user.getAccount().isEnable();
    }

    public String getPassword() {
        return this.password;
    }

    public String getUsername() {
        return this.username;
    }

    @Hidden
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Hidden
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Hidden
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Hidden
    @JsonIgnore
    public boolean isEnabled() {
        return enable;
    }

}
