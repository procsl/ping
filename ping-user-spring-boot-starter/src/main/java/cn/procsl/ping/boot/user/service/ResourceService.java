package cn.procsl.ping.boot.user.service;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.domain.rbac.entity.QResource;
import cn.procsl.ping.boot.user.domain.rbac.entity.Resource;
import cn.procsl.ping.boot.user.domain.rbac.entity.ResourceType;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * 资源服务
 *
 * @author procsl
 * @date 2020/07/05
 */
@Named
@Singleton
@RequiredArgsConstructor
@Validated
@Transactional(rollbackFor = Exception.class)
public class ResourceService {

    @Inject
    final JpaRepository<Resource, Long> resourceJpaRepository;

    final QuerydslPredicateExecutor<Resource> resourceQueryDslPredicateExecutor;

    /**
     * 创建资源
     *
     * @param name     资源名称
     * @param type     资源类型
     * @param parentId 父资源ID
     * @param depends  依赖的资源IDs
     * @return 返回创建成功后的资源ID
     * @throws BusinessException 创建失败抛出此异常
     */
    public Long create(@NotBlank @Size(max = 20) String name, @NotNull ResourceType type, Long parentId, Set<Long> depends) throws BusinessException {
        this.checkNameable(name);

        Resource resource = Resource
                .creator()
                .name(name)
                .parentId(parentId)
                .type(type)
                .depends(depends)
                .done();

        resourceJpaRepository.save(resource);
        return resource.getId();
    }

    /**
     * 删除资源
     *
     * @param resourceId 资源ID
     * @throws BusinessException 资源删除异常
     */
    public void delete(@NotNull Long resourceId) throws BusinessException {

    }

    /**
     * 加载资源
     *
     * @param resourceId 资源ID
     * @return 返回被加载的资源
     * @throws BusinessException 如果加载的资源不存在则抛出异常
     */
    public Resource load(@NotNull Long resourceId) throws BusinessException {
        return resourceJpaRepository.findById(resourceId).orElseThrow(() -> new BusinessException("资源不存在"));
    }

    /**
     * 修改资源类型
     *
     * @param resourceId 资源ID
     * @param type       资源类型
     * @throws BusinessException 当资源未找到时
     */
    public void changeType(@NotNull Long resourceId, @NotNull ResourceType type) throws BusinessException {
        Resource resource = load(resourceId);
        resource.changeType(type);
        resourceJpaRepository.save(resource);
    }

    /**
     * 修改资源父节点
     *
     * @param resourceId 资源ID
     * @param parentId   父资源
     * @throws BusinessException 当资源未找到时
     */
    public void changeParent(@NotNull Long resourceId, Long parentId) throws BusinessException {
        Resource resource = load(resourceId);
        resource.changeParentNode(parentId);
        resourceJpaRepository.save(resource);
    }

    /**
     * 修改名称
     *
     * @param resourceId 资源ID
     * @param name       资源名称
     * @throws BusinessException 当资源未找到时
     */
    public void changeName(@NotNull Long resourceId, @NotBlank @Size(max = 20) String name) throws BusinessException {

        Resource resource = load(resourceId);
        boolean exist = this.existsName(resource, name);
        if (exist) {
            throw new BusinessException("资源名称已存在");
        }

        resource.rename(name);
        resourceJpaRepository.save(resource);
    }

    /**
     * 校验名称是否存在
     *
     * @param name 名称
     * @throws BusinessException 如果资源名称存在抛出异常
     */
    public void checkNameable(@NotNull Long resourceId, @NotBlank @Size(max = 20) String name) throws BusinessException {
        if (existsName(resourceId, name)) {
            throw new BusinessException("资源名称已存在");
        }
    }

    /**
     * 检查全局名称
     *
     * @param name 新名称
     * @throws BusinessException
     */
    public void checkNameable(@NotBlank @Size(max = 20) String name) throws BusinessException {
        boolean exists = resourceQueryDslPredicateExecutor.exists(QResource.resource.name.eq(name));
        if (exists) {
            throw new BusinessException("资源名称已存在");
        }
    }

    /**
     * 检查名称是否存在
     *
     * @param resourceId 资源ID
     * @param name       资源名称
     * @return 如果存在返回true
     */
    public boolean existsName(@NotNull Long resourceId, @NotBlank @Size(max = 20) String name) {

        BooleanExpression eqName = QResource.resource.name.eq(name);

        boolean isEq = this.resourceQueryDslPredicateExecutor.exists(QResource.resource.id.eq(resourceId).and(eqName));
        if (isEq) {
            return false;
        }
        return this.resourceQueryDslPredicateExecutor.exists(eqName);
    }

    /**
     * 检测是否存在指定的资源
     *
     * @param resourceId
     * @throws BusinessException
     */
    public void exists(@NotNull Long resourceId) throws BusinessException {
        boolean exists = resourceJpaRepository.existsById(resourceId);
        if (exists) {
            throw new BusinessException("resource不存在!");
        }
    }

    boolean existsName(Resource resource, String name) {

        boolean isEq = ObjectUtils.nullSafeEquals(name, resource.getName());
        if (isEq) {
            return false;
        }

        BooleanExpression eqName = QResource.resource.name.eq(name);
        return this.resourceQueryDslPredicateExecutor.exists(eqName);
    }

}
