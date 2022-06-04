package cn.procsl.ping.boot.infra.domain.conf;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Optional;

@Indexed
@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class ConfigOptionService {

    final JpaRepository<Config, Long> jpaRepository;
    final JpaSpecificationExecutor<Config> jpaSpecificationExecutor;

    /**
     * 添加配置项
     *
     * @param key     配置项的key
     * @param content 配置项的内容
     */
    public Long put(@NotNull String key, String content) {
        return this.put(key, content, null);
    }

    public Long put(@NotNull String key, String content, String desc) {
        Optional<Config> option = this.jpaSpecificationExecutor.findOne((Specification<Config>) (root, query, cb) -> cb.equal(root.get("key"), key));
        option.ifPresent(item -> item.edit(key, content, desc));
        Config config = option.orElseGet(() -> Config.creator(key, content, desc));
        return this.jpaRepository.save(config).getId();
    }

    /**
     * 获取配置内容
     *
     * @param key 指定的配置项Key
     * @return 如果配置项不存在或者为空, 则返回 null
     */
    @Nullable
    public String get(@NotEmpty String key) {
        Optional<Config> config = this.jpaSpecificationExecutor.findOne((Specification<Config>) (root, query, cb) -> cb.equal(root.get("key"), key));
        return config.map(Config::getContent).orElse(null);
    }
}
