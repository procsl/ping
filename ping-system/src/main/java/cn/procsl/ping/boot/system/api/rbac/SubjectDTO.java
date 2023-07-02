package cn.procsl.ping.boot.system.api.rbac;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class SubjectDTO implements Serializable {

    @Schema(description = "用户ID")
    Long userId;


}
