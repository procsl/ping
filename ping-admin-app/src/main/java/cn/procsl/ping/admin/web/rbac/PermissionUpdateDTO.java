package cn.procsl.ping.admin.web.rbac;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class PermissionUpdateDTO implements Serializable {

    @NotBlank String option;
    @NotBlank String resource;

}
