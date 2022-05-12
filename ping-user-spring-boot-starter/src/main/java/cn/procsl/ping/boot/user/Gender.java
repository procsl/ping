package cn.procsl.ping.boot.user;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(defaultValue = "用户性别")
public enum Gender {

    @Schema(defaultValue = "男")
    man,

    @Schema(defaultValue = "女")
    woman,

    @Schema(defaultValue = "未知")
    unknown,
}
