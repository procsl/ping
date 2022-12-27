package cn.procsl.ping.boot.system.service;

import cn.procsl.ping.boot.system.domain.config.Config;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import java.util.Optional;

@Indexed
@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class ConfigFacade {

    final JpaRepository<Config, Long> jpaRepository;
    final JpaSpecificationExecutor<Config> jpaSpecificationExecutor;

    /**
     * 添加配置项
     *
     * @param name    配置项的key
     * @param content 配置项的内容
     */
    public Long put(@NotNull String name, String content) {
        return this.put(name, content, null);
    }

    public Long put(@NotNull String name, String content, String desc) {
        Optional<Config> option = this.jpaSpecificationExecutor
                .findOne((Specification<Config>) (root, query, cb) -> cb.equal(root.get("name"), name));
        option.ifPresent(item -> item.edit(name, content, desc));
        Config config = option.orElseGet(() -> Config.creator(name, content, desc));
        return this.jpaRepository.save(config).getId();
    }

    /**
     * 获取配置内容
     *
     * @param name 指定的配置项name
     * @return 如果配置项不存在或者为空, 则返回 null
     */
    @Nullable
    public String get(@NotEmpty String name) {
        Optional<Config> config = this.jpaSpecificationExecutor.findOne(
                (root, query, cb) -> cb.equal(root.get("name"), name));
        return config.map(Config::getContent).orElse(null);
    }
}
