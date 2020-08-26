package cn.procsl.ping.boot.domain.business.dictionary.service;

import cn.procsl.ping.boot.domain.business.dictionary.model.DictPath;
import cn.procsl.ping.boot.domain.business.dictionary.model.DictValueDTO;
import cn.procsl.ping.boot.domain.business.dictionary.model.Dictionary;
import cn.procsl.ping.boot.domain.business.dictionary.model.Value;
import cn.procsl.ping.boot.domain.business.dictionary.repository.CustomDictionaryRepository;
import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.business.domain.DomainIdEntity;
import cn.procsl.ping.business.exception.BusinessException;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

import static cn.procsl.ping.boot.domain.business.dictionary.model.Dictionary.SPACE_NAME_LEN;
import static cn.procsl.ping.boot.domain.business.dictionary.repository.CustomDictionaryRepository.dict;

/**
 * 字典服务
 *
 * @author procsl
 * @date 2020年8月24日
 */
@Named
@Singleton
@AllArgsConstructor
@Validated
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class DictionaryService {

    final AdjacencyTreeRepository<Dictionary, Long, DictPath> adjacencyTreeRepository;

    final JpaRepository<Dictionary, Long> jpaRepository;

    final QuerydslPredicateExecutor<Dictionary> querydslPredicateExecutor;

    final CustomDictionaryRepository customDictionaryRepository;


    /**
     * id必须合法(存在于数据库), 否则抛出异常
     * <p>
     * 如果当前节点下已经存在 指定的spaceName,则直接返回spaceName id
     *
     * @param id        被挂载的节点ID
     * @param spaceName 节点名称
     * @return 创建成功后返回创建的 space id
     * @throws IllegalArgumentException 如果参数错误,不合法, 存在非法字符
     * @throws EntityNotFoundException  if id entity not found
     * @throws BusinessException        如果对应的spaceName已存在与当前id的节点下
     */
    @Transactional
    public Long create(@NotNull Long id, @NotBlank @Size(min = 1, max = SPACE_NAME_LEN) String spaceName)
        throws IllegalArgumentException, EntityNotFoundException {

        boolean exists = this.querydslPredicateExecutor.exists(dict.parentId.eq(id).and(dict.space.eq(spaceName)));
        if (exists) {
            throw new BusinessException(spaceName + "已存在于" + id + "节点下");
        }

        // 不存在会抛错
        Dictionary parentSpace = this.jpaRepository.getOne(id);
        Dictionary dic = this.jpaRepository.save(new Dictionary(spaceName));
        dic.changeParent(parentSpace);
        this.jpaRepository.flush();
        log.info("创建数据字典:id={} space={}", dic.getId(), spaceName);
        return dic.getId();
    }

    /**
     * 创建root节点
     *
     * @param path 用于创建的path
     * @return 如果存在或创建成功则返回id
     * @throws IllegalArgumentException 如果参数错误则抛异常
     */
    @Transactional
    public Long create(@NotBlank String path)
        throws IllegalArgumentException, BusinessException {

        if (!path.startsWith(Dictionary.getDelimiter())) {
            path = Dictionary.getDelimiter().concat(path);
        }

        List<String> nodes = Dictionary.split(path);
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("path不可为空");
        }

        // TODO
        Dictionary parent = this.querydslPredicateExecutor.findOne(dict.parentId.eq(dict.id).and(dict.space.eq(nodes.get(0)))).orElse(null);
        for (int i = 0; i < nodes.size(); i++) {
            Dictionary dic = parent == null ? new Dictionary(nodes.get(i)) : new Dictionary(nodes.get(i), parent);
            parent = this.jpaRepository.save(dic);
        }

        Dictionary tmp = this.jpaRepository.saveAndFlush(new Dictionary(path));
        log.info("创建根数据字典,id={},space={}", tmp.getId(), path);
        return tmp.getId();
    }

    /**
     * 禁用
     *
     * @param id 指定id
     * @throws EntityNotFoundException 如果实体未找到
     */
    public void disable(@NotNull Long id) throws EntityNotFoundException {
        log.info("禁用:{}", id);
        this.jpaRepository.getOne(id).setActive(false);
    }

    /**
     * 启用
     *
     * @param id 指定id
     * @throws EntityNotFoundException 如果实体未找到
     */
    public void enable(@NotNull Long id) throws EntityNotFoundException {
        log.info("启用:{}", id);
        this.jpaRepository.getOne(id).setActive(true);
    }

    /**
     * 修改value
     *
     * @param id    指定id
     * @param value 可为null, 新value
     * @throws EntityNotFoundException if entity is not found!
     */
    public void changeValue(@NotNull Long id, Value value) throws EntityNotFoundException {
        if (value == null) {
            value = Dictionary.EMPTY_VALUE;
        }
        this.jpaRepository.getOne(id).setValue(value);
    }

    /**
     * 重命名指定的节点
     *
     * @param id        指定id
     * @param spaceName 新名称
     * @return 旧名称
     * @throws BusinessException if space not found
     */
    @Transactional
    public String rename(@NotNull Long id, @NotBlank @Size(min = 1, max = SPACE_NAME_LEN) String spaceName)
        throws EntityNotFoundException, BusinessException {
        // 首先通过 id == :id and space == :spaceName 匹配
        boolean isEquals = this.querydslPredicateExecutor
            .exists(dict.id.eq(id).and(dict.space.eq(spaceName)));

        if (isEquals) {
            log.info("名称相同,跳过更改:id={},space={}", id, spaceName);
            return spaceName;
        }

        Dictionary dic = this.jpaRepository.getOne(id);
        dic.rename(spaceName);
        this.jpaRepository.flush();
        return dic.getSpace();
    }

    /**
     * 删除指定的节点以及子节点
     *
     * @param id 指定的ID
     * @return 返回删除的节点数
     */
    @Transactional
    public int remove(@NotNull Long id) {
        log.info("删除数据字典:id={}", id);
        return this.adjacencyTreeRepository.remove(id);
    }

    /**
     * 通过指定的path获取值
     * 只获取active is true的值
     *
     * @param path 以全局配置的配置符分割的字符串
     * @return 返回对应路径的值
     * @throws IllegalArgumentException 如果path分隔符不合法
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public DictValueDTO search(@NotNull String path) throws IllegalArgumentException {
        return this.customDictionaryRepository.search(path, false,
            Projections.constructor(DictValueDTO.class, dict.id, dict.value));
    }

    /**
     * 检测是否存在指定的path
     *
     * @param path 全路径
     * @return 如果存在, 则返回true
     */
    public Long contains(@NotNull String path) {
        QBean<DomainIdEntity<Long>> select = Projections.fields(DicId.class, dict.id);
        DomainIdEntity<Long> tmp = this.customDictionaryRepository.search(path, true, select);
        return tmp == null ? null : tmp.getId();
    }


    static protected class DicId extends DomainIdEntity<Long> {
        public DicId(Long aLong) {
            super(aLong);
        }
    }

}
