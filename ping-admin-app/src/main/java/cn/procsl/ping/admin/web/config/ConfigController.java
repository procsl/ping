package cn.procsl.ping.admin.web.config;

import cn.procsl.ping.admin.web.MarkPageable;
import cn.procsl.ping.admin.utils.QueryBuilder;
import cn.procsl.ping.admin.web.FormatPage;
import cn.procsl.ping.boot.infra.domain.conf.Config;
import cn.procsl.ping.boot.infra.domain.conf.ConfigOptionService;
import cn.procsl.ping.boot.infra.domain.conf.QConfig;
import cn.procsl.ping.exception.BusinessException;
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

    @Transactional
    @Operation(summary = "编辑配置项")
    @PatchMapping("/v1/configs/{id}")
    public void edit(@PathVariable Long id, @RequestBody @Validated ConfigDTO config) throws BusinessException {
        this.jpaRepository.getById(id).edit(config.getKey(), config.getContent(), config.getDescription());
    }

    @Transactional
    @PutMapping("/v1/configs")
    @Operation(summary = "创建或更新配置项")
    public Long put(@RequestBody @Validated ConfigDTO config) throws BusinessException {
        return configOptionService.put(config.getKey(), config.getContent(), config.getDescription());
    }


    @Transactional
    @Operation(summary = "删除配置项")
    @DeleteMapping("/v1/configs/{id}")
    public void delete(@PathVariable Long id) throws BusinessException {
        this.jpaRepository.deleteById(id);
    }

    @Nullable
    @GetMapping("/v1/configs/{key}")
    @Transactional(readOnly = true)
    @Operation(summary = "获取配置内容")
    public ConfigKeyValueDTO getConfig(@PathVariable String key) {
        return new ConfigKeyValueDTO(key, this.configOptionService.get(key));
    }

    @Nullable
    @MarkPageable
    @GetMapping("/v1/configs")
    @Transactional(readOnly = true)
    @Operation(summary = "获取配置内容")
    public FormatPage<ConfigVO> findConfig(Pageable pageable, @RequestParam(required = false) String key) {
        QBean<ConfigVO> select = Projections.bean(ConfigVO.class, qconfig.id, qconfig.key, qconfig.content, qconfig.description);

        JPQLQuery<ConfigVO> query = this.queryFactory.select(select).from(qconfig);

        QueryBuilder<ConfigVO> builder = QueryBuilder.builder(query)
                .and(key, () -> qconfig.key.like(String.format("%%%s%%", key)));

        return FormatPage.page(builder, pageable);
    }

}
