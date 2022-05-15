package cn.procsl.ping.boot.infra.conf;


import cn.procsl.ping.boot.domain.valid.UniqueField;
import cn.procsl.ping.boot.domain.valid.UniqueValidation;
import cn.procsl.ping.exception.BusinessException;
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
import javax.validation.constraints.Size;
import java.util.Optional;

@Indexed
@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class ConfigService {

    final UniqueValidation uniqueValidation;
    final JpaRepository<Config, Long> jpaRepository;
    final JpaSpecificationExecutor<Config> jpaSpecificationExecutor;

    /**
     * 添加配置
     *
     * @param key         配置项key 不可与其他配置项重复
     * @param content     配置项内容
     * @param description 配置项描述
     * @return 如果创建成功返回ID
     * @throws BusinessException 如果创建失败
     */
    @Transactional
    public Long add(
            @NotNull @UniqueField(entity = Config.class, fieldName = "key", message = "配置项[{field}]已存在") String key,
            @Size(max = 100) String content,
            @Size(max = 500) String description
    ) throws BusinessException {
        Config config = new Config(key, content, description);
        this.jpaRepository.save(config);
        return config.getId();
    }

    /**
     * 编辑配置项
     *
     * @param id          配置项ID
     * @param key         编辑后的配置项的key, 不可与其他配置项重复
     * @param content     编辑后的内容
     * @param description 编辑后的配置项描述
     * @throws BusinessException 如果编辑失败
     */
    @Transactional
    public void edit(
            @NotNull Long id,
            @NotNull String key,
            @Size(max = 100) String content,
            @Size(max = 500) String description
    ) throws BusinessException {
        this.uniqueValidation.valid(Config.class, id, "key", key, "配置项的Key已存在");
        Config db = this.jpaRepository.getById(id);
        db.edit(key, content, description);
        this.jpaRepository.save(db);
    }


    /**
     * 删除配置项
     *
     * @param id 配置项ID
     * @throws BusinessException 如果删除失败
     */
    @Transactional
    public void delete(@NotNull Long id) throws BusinessException {
        this.jpaRepository.deleteById(id);
    }

    /**
     * 获取配置内容
     *
     * @param key 指定的配置项Key
     * @return 如果配置项不存在或者为空, 则返回 null
     */
    @Nullable
    public String getConfig(@NotEmpty String key) {
        Optional<Config> config = this.jpaSpecificationExecutor.findOne((Specification<Config>) (root, query, cb) -> cb.equal(root.get("key"), key));
        return config.map(Config::getContent).orElse(null);
    }

}
