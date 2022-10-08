package cn.procsl.ping.boot.oa.web;

import cn.procsl.ping.boot.oa.adapter.EmployeeServiceAdapter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class DefaultServiceAdapter implements EmployeeServiceAdapter {

    @Override
    public String currentLoginEmployeeId() {
        return "123456";
    }

    @Override
    public Map<String, String> getEmployeeNames(Collection<String> employeeId) {
        return null;
    }

}
