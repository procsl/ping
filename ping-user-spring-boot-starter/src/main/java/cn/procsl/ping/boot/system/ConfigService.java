package cn.procsl.ping.boot.system;


import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Indexed
@Service
@Validated
@RequiredArgsConstructor
public class ConfigService {

    final JpaRepository<Config, Long> jpaRepository;

    final JpaSpecificationExecutor<Config> jpaSpecificationExecutor;

    @Transactional
    public Long add(@Valid ConfigDTO dto) throws BusinessException {
        Config config = new Config();
        BeanUtils.copyProperties(dto, config);
        this.jpaRepository.save(config);
        return config.getId();
    }

    @Transactional
    public void edit(@NotNull Long id, @Validated ConfigDTO dto) throws BusinessException {
        Config db = this.jpaRepository.getById(id);
        BeanUtils.copyProperties(dto, db);
        this.jpaRepository.save(db);
    }


    @Transactional
    public void delete(@NotNull Long id) throws BusinessException {
        this.jpaRepository.deleteById(id);
    }

    @Nullable
    public String getConfig(@NotEmpty String key) {
        Optional<Config> config = this.jpaSpecificationExecutor.findOne((Specification<Config>) (root, query, cb) -> cb.equal(root.get("key"), key));
        return config.map(Config::getContent).orElse(null);
    }

}
