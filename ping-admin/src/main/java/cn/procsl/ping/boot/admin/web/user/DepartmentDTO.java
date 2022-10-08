package cn.procsl.ping.boot.admin.web.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class DepartmentDTO implements Serializable {

    final Long id;
    final String name;
    final Long employeeId;

}
