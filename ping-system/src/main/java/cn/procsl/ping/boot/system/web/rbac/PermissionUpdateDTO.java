package cn.procsl.ping.boot.system.web.rbac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionUpdateDTO implements Serializable {

    @NotBlank String operate;
    @NotBlank String resource;

}
