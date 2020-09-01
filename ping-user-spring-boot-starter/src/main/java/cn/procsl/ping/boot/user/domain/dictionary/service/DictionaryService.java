package cn.procsl.ping.boot.user.domain.dictionary.service;

import cn.procsl.ping.boot.domain.business.tree.repository.AdjacencyTreeRepository;
import cn.procsl.ping.boot.user.domain.dictionary.model.DictPath;
import cn.procsl.ping.boot.user.domain.dictionary.model.Dictionary;
import cn.procsl.ping.boot.user.domain.dictionary.model.Payload;
import cn.procsl.ping.boot.user.domain.dictionary.repository.CustomDictionaryRepository;
import cn.procsl.ping.business.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

import static cn.procsl.ping.boot.user.domain.dictionary.model.Dictionary.SPACE_NAME_LEN;
import static cn.procsl.ping.boot.user.domain.dictionary.repository.CustomDictionaryRepository.dict;

/**
 * 字典服务
 *
 * @author procsl
 * @date 2020年8月24日
 */
@AllArgsConstructor
@Validated
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class DictionaryService {

    final AdjacencyTreeRepository<Dictionary, Long, DictPath> adjDicRepo;

    final JpaRepository<Dictionary, Long> jpaRepository;

    final QuerydslPredicateExecutor<Dictionary> dictExecutor;

    final CustomDictionaryRepository customRepo;

    /**
     * 通过指定父节点创建
     *
     * @param id       父节点ID
     * @param space    节点space
     * @param payloads 节点数据
     * @return 返回创建成功后的节点ID
     * @throws EntityNotFoundException  如果父节点id不存在
     * @throws IllegalArgumentException 如果space错误,包含非法字符
     */
    public Long create(@NotNull Long id,
                       @NotBlank @Size(min = 1, max = SPACE_NAME_LEN) String space,
                       Payload... payloads) throws EntityNotFoundException,
        IllegalArgumentException {

        Long currentId = this.adjDicRepo.findOne(dict.id, dict.parentId.eq(id).and(dict.space.eq(space)));
        if (currentId != null) {
            return currentId;
        }

        Dictionary parent = this.jpaRepository.getOne(id);
        Dictionary dic = new Dictionary(space, parent, payloads);
        return this.jpaRepository.save(dic).getId();
    }

    /**
     * 通过路径创建
     *
     * @param path     指定分隔符的路径
     * @param payloads 负载/元数据
     * @return 如果创建成功返回ID
     * @throws IllegalArgumentException 如果路径中分段长度不合法,报错
     */
    public Long create(@NotBlank String path, Payload... payloads) throws IllegalArgumentException {
        List<String> paths = Dictionary.split(path);
        for (String s : paths) {
            if (s.length() > SPACE_NAME_LEN) {
                throw new IllegalArgumentException(s + ":space长度超长");
            }
        }

        this.customRepo.search(dict.id, path, true);
        return null;
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
        boolean isEquals = this.dictExecutor
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
        return this.adjDicRepo.remove(id);
    }

}
