package cn.procsl.ping.app.adapter;

import cn.procsl.ping.boot.admin.auth.login.SessionUserDetail;
import cn.procsl.ping.boot.admin.domain.user.User;
import cn.procsl.ping.boot.admin.web.user.DepartService;
import cn.procsl.ping.boot.admin.web.user.DepartmentDTO;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.oa.adapter.EmployeeServiceAdapter;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Indexed;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Indexed
@Component
@RequiredArgsConstructor
public class EmployeeInfoServiceAdapter implements EmployeeServiceAdapter, DepartService {

    final HttpServletRequest request;

    final JpaRepository<User, Long> jpaRepository;

    final EntityManager entityManager;

    final JPQLQueryFactory queryFactory;

    final NamedParameterJdbcTemplate named;


    @Override
    public String currentLoginEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof SessionUserDetail) {
            return ((SessionUserDetail) authentication.getPrincipal()).getId().toString();
        }
        throw new BusinessException("找不到用户");
    }

    @Override
    public Map<String, String> getEmployeeNames(Collection<String> employeeId) {
        List<Long> ids = employeeId.stream().map(Long::parseLong).collect(Collectors.toList());
        List<User> users = this.jpaRepository.findAllById(ids);
        HashMap<String, String> result = new HashMap<>(users.size());
        users.forEach(item -> result.put(Objects.requireNonNull(item.getId()).toString(), item.getName()));
        return result;
    }

    @Override
    public Map<Long, DepartmentDTO> getDepartNames(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("userId", userIds);
        val array = named.query(
                "SELECT d.id, d.name, s.employee_id  FROM o_department_employee as s inner JOIN o_department as d on " +
                        "s.department_id  = d.id where s.employee_id in (:userId) ",
                parameter
                ,
                (rs, rowNum) -> {
                    long id = rs.getLong("id");
                    String name = rs.getString("name");
                    long empId = rs.getLong("employee_id");
                    return new DepartmentDTO(id, name, empId);
                });
        HashMap<Long, DepartmentDTO> result = new HashMap<>(userIds.size());
        array.forEach(item -> result.put(item.getEmployeeId(), item));
        return result;
    }

}
