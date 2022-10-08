package cn.procsl.ping.boot.oa.web.sales;

import cn.procsl.ping.boot.admin.domain.rbac.DataPermissionFilter;
import cn.procsl.ping.boot.common.utils.QueryBuilder;
import cn.procsl.ping.boot.common.web.FormatPage;
import cn.procsl.ping.boot.common.web.MarkPageable;
import cn.procsl.ping.boot.oa.adapter.EmployeeServiceAdapter;
import cn.procsl.ping.boot.oa.domain.sales.EmployeeSales;
import cn.procsl.ping.boot.oa.domain.sales.QEmployeeSales;
import cn.procsl.ping.boot.oa.web.SimpleEmployeeDTO;
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
@Tag(name = "sales", description = "销售信息管理")
public class EmployeeSalesController {

    final JpaRepository<EmployeeSales, Long> jpaRepository;

    final JpaSpecificationExecutor<EmployeeSales> jpaSpecificationExecutor;

    final JPQLQueryFactory queryFactory;

    final QEmployeeSales sales = QEmployeeSales.employeeSales;

    final EmployeeServiceAdapter employeeServiceAdapter;


    @PostMapping("/v1/oa/sales")
    @Operation(summary = "添加销售信息")
    @Transactional(rollbackFor = Exception.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addSales(@RequestBody EmployeeSalesDTO salesDTO) {

        List<EmployeeSales> all = jpaSpecificationExecutor.findAll(
                (root, query, cb) -> {
                    Predicate cd1 = cb.equal(root.get("month"), salesDTO.getMonth());
                    Predicate cd2 = cb.equal(root.get("employeeId"), salesDTO.getEmployeeId());
                    return cb.and(cd1, cd2);
                });
        if (!all.isEmpty()) {
            jpaRepository.deleteAll(all);
        }
        EmployeeSales entity = new EmployeeSales(salesDTO.employeeId, salesDTO.month, salesDTO.money);
        this.jpaRepository.save(entity);
    }

    @PatchMapping("/v1/oa/sales/{id}")
    @Operation(summary = "修改销售信息")
    @Transactional(rollbackFor = Exception.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeSales(@PathVariable Long id, @RequestBody EmployeeSalesDTO sales) {
        this.jpaRepository.getReferenceById(id).changeSales(sales.getMoney());
    }

    @DeleteMapping("/v1/oa/sales/{id}")
    @Operation(summary = "删除销售信息")
    @Transactional(rollbackFor = Exception.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSales(@PathVariable Long id) {
        this.jpaRepository.deleteById(id);
    }

    @MarkPageable
    @GetMapping("/v1/oa/sales")
    @Operation(summary = "获取销售信息")
    @Transactional(readOnly = true)
    @DataPermissionFilter(filter = "#root[currentUserDataPermission]?.apply('读取-所有用户销售信息权限')",
            executor = "#root[overrideArgument]?.apply(#arguments, #root[currentAccount].get()?.id + '', 0)")
    public FormatPage<EmployeeSalesVO> findSales(@RequestParam(name = "employeeId", required = false) String id,
                                                 Pageable pageable) {
        QBean<EmployeeSalesVO> projections = Projections.bean(EmployeeSalesVO.class,
                sales.id,
                sales.month,
                sales.money,
                sales.employeeId);

        JPQLQuery<EmployeeSalesVO> query = queryFactory.select(projections)
                                                       .from(sales)
                                                       .orderBy(sales.employeeId.asc(), sales.month.desc());

        val builder = QueryBuilder
                .builder(query)
                .and(id, () -> sales.employeeId.eq(id));

        FormatPage<EmployeeSalesVO> tmp = FormatPage.page(builder, pageable);

        Collection<String> employeeIds = tmp.convert(EmployeeSalesVO::getEmployeeId);
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
