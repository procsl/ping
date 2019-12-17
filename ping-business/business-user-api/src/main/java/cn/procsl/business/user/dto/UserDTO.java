package cn.procsl.business.user.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
        String credential;

        /**
         * 用户密码
         */
        @NotNull
        @NotEmpty
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
