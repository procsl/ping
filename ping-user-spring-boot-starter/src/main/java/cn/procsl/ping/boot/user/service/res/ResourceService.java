package cn.procsl.ping.boot.user.service.res;

import cn.procsl.ping.boot.data.business.BusinessException;
import cn.procsl.ping.boot.user.command.res.ChangeNameCommand;
import cn.procsl.ping.boot.user.command.res.ChangeParentCommand;
import cn.procsl.ping.boot.user.command.res.ChangeTypeCommand;
import cn.procsl.ping.boot.user.command.res.CreateCommand;
import cn.procsl.ping.boot.user.domain.res.entity.QResource;
import cn.procsl.ping.boot.user.domain.res.entity.Resource;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static cn.procsl.ping.boot.user.domain.res.entity.ResourceTreeNode.isRoot;

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
    final protected JpaRepository<Resource, Long> resourceJpaRepository;

    @Inject
    final protected QuerydslPredicateExecutor<Resource> resourceQueryDslPredicateExecutor;

    /**
     * 创建资源
     *
     * @param command 创建资源
     * @return 返回创建成功后的资源ID
     * @throws BusinessException 创建失败抛出此异常
     */
    public Long create(@NonNull @Valid CreateCommand command) throws BusinessException {
        this.checkNameable(command.getName());

        Resource resource = Resource
                .creator()
                .name(command.getName())
                .parentId(command.getParentId())
                .type(command.getType())
                .depends(command.getDepends())
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
        resourceJpaRepository.deleteById(resourceId);
    }

    Resource load(@NotNull Long resourceId) throws BusinessException {
        return resourceJpaRepository.findById(resourceId).orElseThrow(() -> new BusinessException("资源不存在"));
    }

    /**
     * 修改资源类型
     *
     * @param command 修改资源类型
     * @throws BusinessException 当资源未找到时
     */
    public void changeType(@NonNull @Valid ChangeTypeCommand command) throws BusinessException {
        Resource resource = load(command.getResourceId());
        if (command.getType() == resource.getType()) {
            return;
        }
        resource.changeType(command.getType());
        resourceJpaRepository.save(resource);
    }

    /**
     * 修改资源父节点
     *
     * @param command 修改资源节点
     * @throws BusinessException 当资源未找到时
     */
    public void changeParent(@NonNull @Valid ChangeParentCommand command) throws BusinessException {
        Resource resource = load(command.getResourceId());

        do {
            if (ObjectUtils.nullSafeEquals(resource.getNode().getParentId(), command.getParentId())) {
                return;
            }

            if (isRoot(command.getParentId())) {
                break;
            }

            if (!this.resourceJpaRepository.existsById(command.getParentId())) {
                throw new BusinessException("parent 不存在");
            }

        } while (false);

        resource.changeParentNode(command.getParentId());

        resourceJpaRepository.save(resource);
    }

    /**
     * 修改名称
     *
     * @param command 修改名称对象
     * @throws BusinessException 当资源未找到时
     */
    public void changeName(@NonNull @Valid ChangeNameCommand command) throws BusinessException {

        Resource resource = load(command.getResourceId());
        if (resource.getName().equals(command.getName())) {
            return;
        }

        this.checkNameable(command.getName());

        resource.rename(command.getName());
        resourceJpaRepository.save(resource);
    }

    /**
     * 校验名称是否存在
     *
     * @param command 名称
     * @throws BusinessException 如果资源名称存在抛出异常
     */
    public void checkNameable(@NonNull @Valid ChangeNameCommand command) throws BusinessException {
        if (existsName(command)) {
            throw new BusinessException("资源名称已存在");
        }
    }

    /**
     * 检查全局名称
     *
     * @param name 新名称
     * @throws BusinessException
     */
    public void checkNameable(@NotBlank @Size(max = 20, min = 1) String name) throws BusinessException {
        boolean exists = resourceQueryDslPredicateExecutor.exists(QResource.resource.name.eq(name));
        if (exists) {
            throw new BusinessException("资源名称已存在");
        }
    }

    /**
     * 检查名称是否存在
     *
     * @param command 资源名称
     * @return 如果存在返回true
     */
    public boolean existsName(@Valid @NonNull ChangeNameCommand command) {

        BooleanExpression eqName = QResource.resource.name.eq(command.getName());

        boolean isEq = this.resourceQueryDslPredicateExecutor.exists(QResource.resource.id.eq(command.getResourceId()).and(eqName));
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
        if (!exists) {
            throw new BusinessException("resource不存在!");
        }
    }

}
