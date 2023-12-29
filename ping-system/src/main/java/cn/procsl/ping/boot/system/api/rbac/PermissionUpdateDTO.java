package cn.procsl.ping.boot.system.api.rbac;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionUpdateDTO implements Serializable {

    @NotBlank String operate;
    @NotBlank String resource;

}
