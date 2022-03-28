package cn.procsl.ping.boot.sys;


import cn.procsl.ping.boot.domain.business.Configuration;
import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Indexed
@Service
@RequiredArgsConstructor
public class ConfigureService {

    final JpaRepository<ConfigurationEntity, Long> configureLongJpaRepository;

    public void createConfigure(@NotNull Configuration configure) throws BusinessException {
        Example<ConfigurationEntity> keyExample = Example.of(ConfigurationEntity.builder().key(configure.getKey()).build());
        if (this.configureLongJpaRepository.exists(keyExample)) {
            throw new BusinessException("配置项名称已存在");
        }
        this.changeConfigure(configure);
    }

    public void changeConfigure(@NotNull Configuration configure) throws BusinessException {
        ConfigurationEntity entity = new ConfigurationEntity();
        BeanUtils.copyProperties(configure, entity, "id");
        configureLongJpaRepository.save(entity);
    }

    public void deleteConfigure(@NotNull String key) throws BusinessException {
        Optional<ConfigurationEntity> option = configureLongJpaRepository.findOne(Example.of(ConfigurationEntity.builder().key(key).build()));
        option.ifPresent(configureLongJpaRepository::delete);
        option.orElseThrow(() -> new BusinessException("配置项未找到"));
    }

}
