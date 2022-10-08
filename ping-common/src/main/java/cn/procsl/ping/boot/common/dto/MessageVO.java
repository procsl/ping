package cn.procsl.ping.boot.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
@Schema(description = "提示信息")
public class MessageDTO implements Serializable {

    @Schema(description = "错误信息或提示信息")
    @NonNull
    final String message;

}
