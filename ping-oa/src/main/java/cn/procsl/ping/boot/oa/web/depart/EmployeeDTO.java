package cn.procsl.ping.boot.oa.web.depart;

import lombok.Data;

import java.io.Serializable;

@Data
public class EmployeeDTO implements Serializable {


    String id;

    String name;

    public EmployeeDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
