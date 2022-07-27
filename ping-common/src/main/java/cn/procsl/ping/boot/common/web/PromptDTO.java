package cn.procsl.ping.boot.common.web;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
@Schema(description = "提示信息")
public class PromptDTO implements Serializable {

    @Schema(description = "提示信息")
    final String message;

}
