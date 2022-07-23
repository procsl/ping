package cn.procsl.ping.boot.admin.web.rbac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionUpdateDTO implements Serializable {

    @NotBlank String operate;
    @NotBlank String resource;

}
