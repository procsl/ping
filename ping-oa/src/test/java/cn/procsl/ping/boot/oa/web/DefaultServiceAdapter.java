package cn.procsl.ping.boot.oa.web;

import cn.procsl.ping.boot.oa.adapter.EmployeeServiceAdapter;
import org.springframework.stereotype.Component;

@Component
public class DefaultServiceAdapter implements EmployeeServiceAdapter {

    @Override
    public String getEmployeeId() {
        return "123456";
    }

}
