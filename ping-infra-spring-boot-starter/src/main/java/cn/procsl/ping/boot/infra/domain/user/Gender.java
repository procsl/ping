package cn.procsl.ping.boot.infra.domain.user;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户性别")
public enum Gender {

    @Schema(description = "男")
    man,

    @Schema(description = "女")
    woman,

    @Schema(description = "未知")
    unknown,
}
