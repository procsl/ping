package cn.procsl.ping.boot.admin.web.user;

import java.util.Collection;
import java.util.Map;

public interface DepartService {

    Map<Long, DepartmentDTO> getDepartNames(Collection<Long> id);

}
