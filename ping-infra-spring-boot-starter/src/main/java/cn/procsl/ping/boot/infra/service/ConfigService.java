package cn.procsl.ping.boot.infra.service;

import cn.procsl.ping.boot.domain.valid.UniqueField;
import cn.procsl.ping.boot.infra.domain.conf.Config;
import cn.procsl.ping.exception.BusinessException;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public interface ConfigService {
    Long add(@NotNull @UniqueField(entity = Config.class, fieldName = "key", message = "配置项[{field}]已存在") String key, @Size(max = 100) String content, @Size(max = 500) String description) throws BusinessException;

    void edit(@NotNull Long id, @NotNull String key, @Size(max = 100) String content, @Size(max = 500) String description) throws BusinessException;

    void delete(@NotNull Long id) throws BusinessException;

    @Nullable
    String getConfig(@NotEmpty String key);
}
