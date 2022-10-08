package cn.procsl.ping.boot.oa.web;

import lombok.Data;

import java.io.Serializable;

@Data
public class SimpleEmployeeDTO implements Serializable {

    String employeeId;

    String name;

    public SimpleEmployeeDTO(String employeeId, String name) {
        this.employeeId = employeeId;
        this.name = name;
    }
}
