package cn.procsl.ping.boot.user.domain.rbac.service;

import cn.procsl.ping.boot.user.domain.common.service.AbstractService;
import cn.procsl.ping.boot.user.domain.rbac.model.QSubject;
import cn.procsl.ping.boot.user.domain.rbac.model.Role;
import cn.procsl.ping.boot.user.domain.rbac.model.Subject;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Collection;
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
     * 关联用户
     *
     * @param names 角色路径 最多100个角色
     * @return 返回创建成功后的实体
     * @throws IllegalArgumentException 错误的参数
     */
    @Transactional
    public Long bind(@NotEmpty @Size(min = 1, max = 32) String userId, @Size(max = 100) Collection<String> names) throws IllegalArgumentException {
        Subject subject = this.querydslRepository.findOne(S.userId.eq(userId)).orElse(null);
        Collection<Role> roles = roleService.searchByNames(names);
        if (subject != null) {
            subject.changeRoles(roles);
            subject = jpaRepository.save(subject);
        } else {
            subject = jpaRepository.save(new Subject(userId, roles));
        }
        return subject.getId();
    }

    /**
     * 通过指定IDs关联用户
     *
     * @param roleIds 角色ID
     * @return 返回创建成功之后的实体ID
     * @throws IllegalArgumentException 如果实体参数错误
     */
    @Transactional
    public Long bindByRoleIds(@NotEmpty @Size(min = 1, max = 32) String userId, @Size(max = 100) Collection<Long> roleIds) throws IllegalArgumentException {
        Subject subject = this.querydslRepository.findOne(S.userId.eq(userId)).orElse(null);

        Function<Long, Role> convert = this.roleService::getOne;
        Collection<Role> roles = this.roleService.findByIds(roleIds);
        if (subject != null) {
            subject.changeRoles(roles);
            subject = jpaRepository.save(subject);
        } else {
            subject = jpaRepository.save(new Subject(userId, roles));
        }

        return subject.getId();
    }

}
