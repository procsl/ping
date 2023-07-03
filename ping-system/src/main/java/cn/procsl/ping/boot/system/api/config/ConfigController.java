package cn.procsl.ping.boot.system.api.config;

import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.system.domain.config.Config;
import cn.procsl.ping.boot.system.service.ConfigFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Indexed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Indexed
@RestController
@RequiredArgsConstructor
@Tag(name = "Config", description = "系统配置接口")
public class ConfigController {

    final ConfigFacade configFacade;

    final JpaRepository<Config, Long> jpaRepository;

    @Operation(summary = "编辑配置项")
    @PutMapping(path = "/v1/system/configs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(rollbackFor = Exception.class)
    public void edit(@PathVariable Long id, @RequestBody @Validated ConfigDTO config) throws BusinessException {
        this.jpaRepository.getReferenceById(id)
                .edit(config.getName(), config.getContent(), config.getDescription());
    }

    @Operation(summary = "创建或更新配置项")
    @PostMapping(path = "/v1/system/configs")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional(rollbackFor = Exception.class)
    public void put(@RequestBody @Validated ConfigDTO config) throws BusinessException {
        configFacade.put(config.getName(), config.getContent(), config.getDescription());
    }


    @Operation(summary = "删除配置项")
    @DeleteMapping(path = "/v1/system/configs/{id}")
    @Transactional(rollbackFor = Exception.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws BusinessException {
        this.jpaRepository.deleteById(id);
    }

    @Operation(summary = "获取配置内容")
    @GetMapping(path = "/v1/system/configs/{name}")
    @Transactional(rollbackFor = Exception.class, readOnly = true)
    public ConfigNameValueDTO getConfig(@PathVariable String name) {
        return new ConfigNameValueDTO(name, this.configFacade.get(name));
    }

//    @MarkPageable
//    @Query(path = "/v1/system/configs", summary = "获取配置内容")
//    public FormatPage<ConfigVO> findConfig(Pageable pageable, @RequestParam(required = false) String name) {
//        QBean<ConfigVO> select = Projections.fields(ConfigVO.class, qconfig.id, qconfig.name, qconfig.content,
//                qconfig.description);
//
//        JPQLQuery<ConfigVO> query = this.queryFactory.select(select).from(qconfig);
//
//        QueryBuilder<ConfigVO> builder = QueryBuilder.builder(query)
//                                                     .and(name, () -> qconfig.name.like(String.format("%%%s%%", name)));
//
//        return FormatPage.page(builder, pageable);
//    }

}
