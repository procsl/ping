package cn.procsl.ping.boot.oa.web.depart;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DepartmentDTO implements Serializable {

    Long id;

    @NotNull
    @NotEmpty
    String name;

}
