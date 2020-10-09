package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.domain.business.utils.PathUtils;
import cn.procsl.ping.boot.user.domain.common.AbstractService;
import cn.procsl.ping.boot.user.domain.rbac.model.QSubject;
import cn.procsl.ping.boot.user.domain.rbac.model.Role;
import cn.procsl.ping.boot.user.domain.rbac.model.Subject;
import cn.procsl.ping.business.exception.BusinessException;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Transactional(rollbackFor = Exception.class)
public class SubjectService
    extends AbstractService<Long, Subject> {

    public static final QSubject S = QSubject.subject;

    @Inject
    @Setter
    RoleService roleService;

    public SubjectService(JpaRepository<Subject, Long> jpaRepository,
                          QuerydslPredicateExecutor<Subject> querydslRepository) {
        super(jpaRepository, querydslRepository);
    }

    @Inject
    public SubjectService(JpaRepository<Subject, Long> jpaRepository,
                          QuerydslPredicateExecutor<Subject> querydslRepository,
                          RoleService roleService) {
        this(jpaRepository, querydslRepository);
        this.roleService = roleService;
    }

    /**
     * 创建实体
     *
     * @param rolePaths 角色路径 最多100个角色
     * @return 返回创建成功后的实体
     * @throws IllegalArgumentException 错误的参数
     */
    @Transactional
    public Long create(@Size(max = 100) Collection<String> rolePaths) throws IllegalArgumentException {

        List<String> paths = PathUtils.distinct(rolePaths, Role.DELIMITER);
        Map<String, Role> roles = roleService.search(paths, BusinessException::ifNotFound);
        return jpaRepository.save(new Subject(roles.values())).getId();
    }

    /**
     * 通过指定IDs创建实体
     *
     * @param roleIds 角色ID
     * @return 返回创建成功之后的实体ID
     * @throws IllegalArgumentException 如果实体参数错误
     */
    @Transactional
    public Long createByRoleId(@Size(max = 100) Collection<Long> roleIds) throws IllegalArgumentException {
        Function<Long, Role> convert = this.roleService::getOne;
        Collection<Role> roles = this.roleService.distinctAndConvert(roleIds, convert);
        return jpaRepository.save(new Subject(roles)).getId();
    }

    /**
     * 修改角色
     *
     * @param subjectId 指定的实体
     * @param rolePaths 角色路径 至少一个角色, 最多100个角色
     * @throws EntityNotFoundException 如果指定的实体未找到
     */
    @Transactional
    public void changeRole(@NotNull Long subjectId, @NotNull @Size(min = 1, max = 100) Collection<String> rolePaths) throws EntityNotFoundException {

        List<String> paths = PathUtils.distinct(rolePaths, Role.DELIMITER);
        Map<String, Role> roles = roleService.search(paths, BusinessException::ifNotFound);

        Subject subject = jpaRepository.getOne(subjectId);
        subject.changeRoles(roles.values());
        jpaRepository.save(subject);
    }

    /**
     * 修改角色
     *
     * @param subjectId 实体ID
     * @param roleIds   角色ID
     */
    @Transactional
    public void changeRoleById(@NotNull Long subjectId, @NotNull @Size(min = 1, max = 100) Collection<Long> roleIds) {
        Function<Long, Role> convert = roleService::getOne;
        Collection<Role> roles = this.roleService.distinctAndConvert(roleIds, convert);

        Subject subject = jpaRepository.getOne(subjectId);
        subject.changeRoles(roles);
        jpaRepository.save(subject);
    }

}
