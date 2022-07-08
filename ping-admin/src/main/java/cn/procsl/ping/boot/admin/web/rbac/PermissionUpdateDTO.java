package cn.procsl.ping.boot.admin.web.rbac;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class PermissionUpdateDTO implements Serializable {

    @NotBlank String operate;
    @NotBlank String resource;

}
