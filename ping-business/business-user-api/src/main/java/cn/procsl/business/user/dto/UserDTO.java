package cn.procsl.business.user.dto;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 用户对象
 *
 * @author procsl
 * @date 2019/12/11
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {

    /**
     * 账户姓名
     */
    @NotEmpty
    @Size(max = 20)
    protected String name;

    /**
     * 性别
     */
    @NotNull
    protected Gender gender;

    /**
     * 用户账户
     */
    @NotNull
    @Valid
    protected Account account;

    /**
     * 用户设置信息
     */
    protected Setting setting;

    protected Status status;

    public static enum Status {
        enable, disable
    }

    public static enum Gender {
        男, 女, 未知
    }

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Account {
        /**
         * 用户账号标识符
         */
        @NotNull
        @NotEmpty
        @Size(max = 50)
        String credential;

        /**
         * 用户密码
         */
        @NotNull
        @NotEmpty
        @Size(min = 8, max = 16)
        String password;

        /**
         * 类型
         */
        @NotNull
        Type type;

        public enum Type {
            account, phone, email
        }
    }

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Setting {
        protected Theme theme;

        public enum Theme {
            black, red
        }
    }

}
