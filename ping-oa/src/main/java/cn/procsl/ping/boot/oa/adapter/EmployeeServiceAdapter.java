package cn.procsl.ping.boot.oa.adapter;

import java.util.Collection;
import java.util.Map;

public interface EmployeeServiceAdapter {

    String currentLoginEmployeeId();

    Map<String, String> getEmployeeNames(Collection<String> employeeId);

}
