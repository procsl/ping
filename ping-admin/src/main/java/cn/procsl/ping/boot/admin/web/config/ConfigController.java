package cn.procsl.ping.boot.admin.web.config;

import cn.procsl.ping.boot.admin.domain.conf.Config;
import cn.procsl.ping.boot.admin.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.admin.domain.conf.QConfig;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.common.utils.QueryBuilder;
import cn.procsl.ping.boot.common.web.*;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;


@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "config", description = "系统配置接口")
public class ConfigController {

    final ConfigOptionService configOptionService;

    final JpaRepository<Config, Long> jpaRepository;

    final JPQLQueryFactory queryFactory;

    final QConfig qconfig = QConfig.config;

    @Changed(path = "/v1/configs/{id}", summary = "编辑配置项")
    public void edit(@PathVariable Long id, @RequestBody @Validated ConfigDTO config) throws BusinessException {
        this.jpaRepository.getReferenceById(id)
                          .edit(config.getName(), config.getContent(), config.getDescription());
    }

    @Transactional
    @PutMapping("/v1/configs")
    @Operation(summary = "创建或更新配置项")
    public Long put(@RequestBody @Validated ConfigDTO config) throws BusinessException {
        return configOptionService.put(config.getName(), config.getContent(), config.getDescription());
    }


    @Deleted(path = "/v1/configs/{id}", summary = "删除配置项")
    public void delete(@PathVariable Long id) throws BusinessException {
        this.jpaRepository.deleteById(id);
    }

    @Nullable
    @QueryDetails(path = "/v1/configs/{name}", summary = "获取配置内容")
    public ConfigNameValueDTO getConfig(@PathVariable String name) {
        return new ConfigNameValueDTO(name, this.configOptionService.get(name));
    }

    @MarkPageable
    @QueryDetails(path = "/v1/configs", summary = "获取配置内容")
    public FormatPage<ConfigVO> findConfig(Pageable pageable, @RequestParam(required = false) String name) {
        QBean<ConfigVO> select = Projections.fields(ConfigVO.class, qconfig.id, qconfig.name, qconfig.content,
                qconfig.description);

        JPQLQuery<ConfigVO> query = this.queryFactory.select(select).from(qconfig);

        QueryBuilder<ConfigVO> builder = QueryBuilder.builder(query)
                                                     .and(name, () -> qconfig.name.like(String.format("%%%s%%", name)));

        return FormatPage.page(builder, pageable);
    }

}
