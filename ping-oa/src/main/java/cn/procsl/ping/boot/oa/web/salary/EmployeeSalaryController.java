package cn.procsl.ping.boot.oa.web.salary;


import cn.procsl.ping.boot.admin.domain.rbac.DataPermissionFilter;
import cn.procsl.ping.boot.common.utils.QueryBuilder;
import cn.procsl.ping.boot.common.web.FormatPage;
import cn.procsl.ping.boot.common.web.MarkPageable;
import cn.procsl.ping.boot.oa.adapter.EmployeeServiceAdapter;
import cn.procsl.ping.boot.oa.domain.salary.EmployeeSalary;
import cn.procsl.ping.boot.oa.domain.salary.QEmployeeSalary;
import cn.procsl.ping.boot.oa.web.SimpleEmployeeDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Predicate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "salary", description = "工资管理")
public class EmployeeSalaryController {

    final JpaRepository<EmployeeSalary, Long> jpaRepository;

    final JpaSpecificationExecutor<EmployeeSalary> jpaSpecificationExecutor;

    final JPQLQueryFactory queryFactory;

    final SalaryMapper salaryMapper = Mappers.getMapper(SalaryMapper.class);

    final QEmployeeSalary salary = QEmployeeSalary.employeeSalary;

    final EmployeeServiceAdapter employeeServiceAdapter;


    @PostMapping("/v1/oa/salary")
    @Operation(summary = "添加工资信息")
    @Transactional(rollbackFor = Exception.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addSalary(@RequestBody EmployeeSalaryDTO salaryDTO) {

        List<EmployeeSalary> all = jpaSpecificationExecutor.findAll(
                (root, query, cb) -> {
                    Predicate cd1 = cb.equal(root.get("month"), salaryDTO.getMonth());
                    Predicate cd2 = cb.equal(root.get("employeeId"), salaryDTO.getEmployeeId());
                    return cb.and(cd1, cd2);
                });
        if (!all.isEmpty()) {
            jpaRepository.deleteAll(all);
        }
        EmployeeSalary entity = this.salaryMapper.mapper(salaryDTO);
        this.jpaRepository.save(entity);
    }

    @PatchMapping("/v1/oa/salary/{id}")
    @Operation(summary = "修改工资信息")
    @Transactional(rollbackFor = Exception.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeSalary(@PathVariable Long id, @RequestBody EmployeeSalaryDTO salaryDTO) {
        this.jpaRepository.getReferenceById(id).editSalary(
                salaryDTO.getBasicWage(),
                salaryDTO.getOvertimeWage(),
                salaryDTO.getSalesMoneyWage(),
                salaryDTO.getTrafficWage(),
                salaryDTO.getKaoQinReduce(),
                salaryDTO.getSecureReduce(),
                salaryDTO.getTaxReduce()
        );
    }

    @DeleteMapping("/v1/oa/salary/{id}")
    @Operation(summary = "删除工资信息")
    @Transactional(rollbackFor = Exception.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSalary(@PathVariable Long id) {
        this.jpaRepository.deleteById(id);
    }

    @MarkPageable
    @GetMapping("/v1/oa/salary")
    @Operation(summary = "获取工资信息")
    @Transactional(readOnly = true)
    @DataPermissionFilter(filter = "#root[currentUserDataPermission]?.apply('读取-所有用户工资信息权限')",
            executor = "#root[overrideArgument]?.apply(#arguments, #root[currentAccount].get()?.id + '', 0)")
    public FormatPage<EmployeeSalaryVO> findSalary(@RequestParam(name = "employeeId", required = false) String id,
                                                   Pageable pageable) {
        QBean<EmployeeSalaryVO> projections = Projections.bean(EmployeeSalaryVO.class,
                salary.id,
                salary.basicWage,
                salary.month,
                salary.kaoQinReduce,
                salary.overtimeWage,
                salary.taxReduce,
                salary.salesMoneyWage,
                salary.secureReduce,
                salary.trafficWage,
                salary.employeeId);

        JPQLQuery<EmployeeSalaryVO> query = queryFactory.select(projections)
                                                        .from(salary)
                                                        .orderBy(salary.employeeId.asc(), salary.month.desc());

        val builder = QueryBuilder
                .builder(query)
                .and(id, () -> salary.employeeId.eq(id));

        FormatPage<EmployeeSalaryVO> tmp = FormatPage.page(builder, pageable);

        Collection<String> employeeIds = tmp.convert(EmployeeSalaryVO::getEmployeeId);
        Map<String, String> employeeNames = this.employeeServiceAdapter.getEmployeeNames(employeeIds);


        return tmp.transform(item -> {
            String name = employeeNames.get(item.getEmployeeId());
            if (!ObjectUtils.isEmpty(name)) {
                item.setEmployee(new SimpleEmployeeDTO(item.getEmployeeId(), name));
            }
            return item;
        });
    }

}
