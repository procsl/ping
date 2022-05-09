package cn.procsl.ping.boot.system;


import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Indexed
@Service
@RequiredArgsConstructor
public class ConfigService {

    final JpaRepository<Config, Long> jpaRepository;

    final JpaSpecificationExecutor<Config> jpaSpecificationExecutor;

    @Transactional
    public Long create(@NotNull @Validated Config configure) throws BusinessException {

//        checkKeyExists(null, configure.getKey());
//        configure.setId(null);

        this.jpaRepository.save(configure);
        return configure.getId();
    }

    @Transactional(readOnly = true)
    public boolean checkKeyExists(Long id, String key) {
        {
            long exists = this.jpaSpecificationExecutor.count(ConfigSpec.matchByKey(key));
            // 不存在 key时直接返回
            if (exists <= 0) {
                return false;
            }

            // ID为null 时直接返回 已存在
            if (id == null) {
                return true;
            }
        }

        // 存在ID时才检测 id 和 key是否同时存在
        Specification<Config> spec = ConfigSpec.matchByKey(key).and(ConfigSpec.matchById(id));
        long exists = this.jpaSpecificationExecutor.count(spec);
        return exists > 0;
    }

    @Transactional
    public Long changeConfigure(@NotNull @Validated Config configure) throws BusinessException {

        assert configure.getId() != null;

        boolean exists = this.checkKeyExists(configure.getId(), configure.getKey());
        if (exists) {
            throw new BusinessException("配置项名称已存在");
        }

        Config db = this.jpaRepository.getById(configure.getId());

        BeanUtils.copyProperties(configure, db, "id", "version");
        return this.jpaRepository.save(configure).getId();
    }


    @Transactional
    public void deleteConfigure(@NotNull Long id) throws BusinessException {
        this.jpaRepository.deleteById(id);
    }

    public Optional<Config> getConfigByKey(String key) {
        return jpaRepository.findOne(Example.of(Config.builder().key(key).build()));
    }
}
