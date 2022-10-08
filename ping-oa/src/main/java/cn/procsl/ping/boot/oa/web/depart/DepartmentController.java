package cn.procsl.ping.boot.oa.web.depart;

import cn.procsl.ping.boot.common.utils.QueryBuilder;
import cn.procsl.ping.boot.common.web.FormatPage;
import cn.procsl.ping.boot.common.web.MarkPageable;
import cn.procsl.ping.boot.oa.adapter.EmployeeServiceAdapter;
import cn.procsl.ping.boot.oa.domain.depart.Department;
import cn.procsl.ping.boot.oa.domain.depart.QDepartment;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "department", description = "部门管理")
public class DepartmentController {

    final JpaRepository<Department, Long> jpaRepository;

    final JpaSpecificationExecutor<Department> jpaSpecificationExecutor;

    final JPQLQueryFactory queryFactory;

    final QDepartment dep = QDepartment.department;

    final EntityManager entityManager;

    final EmployeeServiceAdapter employeeServiceAdapter;

    @PostMapping("/v1/oa/departments")
    @Operation(summary = "添加部门")
    @Transactional(rollbackFor = Exception.class)
    public void addDepart(@RequestBody @Validated DepartmentDTO departmentDTO) {
        Department depart = new Department(departmentDTO.getName(), Collections.emptyList());
        this.jpaRepository.save(depart);
    }

    @DeleteMapping("/v1/oa/departments/{id}")
    @Operation(summary = "删除部门")
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepart(@PathVariable Long id) {
        this.jpaRepository.deleteById(id);
    }

    @MarkPageable
    @Transactional(readOnly = true)
    @GetMapping("/v1/oa/departments")
    @Operation(summary = "查询部门数据")
    public FormatPage<DepartmentDTO> getDepartments(Pageable pageable,
                                                    @RequestParam(required = false) String name) {
        QBean<DepartmentDTO> projections = Projections.bean(DepartmentDTO.class, dep.id, dep.name);

        JPQLQuery<DepartmentDTO> query = queryFactory.select(projections)
                                                     .from(dep);

        val builder = QueryBuilder
                .builder(query)
                .and(name, () -> dep.name.like(String.format("%%%s%%", name)));

        return FormatPage.page(builder, pageable);
    }

    @Transactional(readOnly = true)
    @GetMapping("/v1/oa/departments/{id}/employees")
    @Operation(summary = "获取部门员工信息")
    public Collection<EmployeeDTO> departEmployees(@PathVariable Long id) {
        Department depart = this.jpaRepository.getReferenceById(id);
        Collection<String> result = depart.getEmployeeId();
        Map<String, String> names = this.employeeServiceAdapter.getEmployeeNames(result);
        ArrayList<EmployeeDTO> list = new ArrayList<>();
        names.forEach((k, v) -> list.add(new EmployeeDTO(k, v)));
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/v1/oa/departments/{id}/employees")
    @Operation(summary = "添加部门员工")
    public void addDepartEmployees(@PathVariable Long id, @RequestBody Collection<String> employeeIds) {
        for (String employeeId : employeeIds) {
            this.deleteDepartEmployees(employeeId);
        }
        Department department = this.jpaRepository.getReferenceById(id);
        department.addEmployee(employeeIds);
    }

    @Operation(summary = "删除部门员工")
    @DeleteMapping("/v1/oa/departments/employees/{id}")
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartEmployees(@PathVariable String id) {

        TypedQuery<Department> query = this.entityManager.createQuery(
                "select d from Department as d where :id member of d.employeeId ", Department.class);
        query.setParameter("id", id);
        List<Department> list = query.getResultList();
        if (!list.isEmpty()) {
            list.forEach(item -> item.removeEmployee(id));
        }

    }

}
