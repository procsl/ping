package cn.procsl.ping.boot.infra.user;

import cn.procsl.ping.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Optional;

@Indexed
@Service
@RequiredArgsConstructor
public class UserService {

    final AccountFacade accountFacade;

    final AccessControlFacade accessControlFacade;

    final JpaRepository<User, Long> jpaRepository;

    final JpaSpecificationExecutor<User> jpaSpecificationExecutor;

    /**
     * 用户注册, 用户名称默认为账户名称
     *
     * @param account  账户名称
     * @param password 用户密码
     * @throws BusinessException 如果注册失败
     */
    @Transactional
    public Long register(@NotBlank @Size(min = 5, max = 20) String account, @NotBlank @Size(min = 5, max = 20) String password) throws BusinessException {
        Long accountId = accountFacade.create(account, password);
        User user = this.jpaRepository.save(new User(account, accountId));
        accessControlFacade.grantDefaultRoles(user.getId());
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param account  账户名称
     * @param password 密码
     * @return 返回用户信息
     * @throws BusinessException 如果登录失败
     */
    @Transactional(readOnly = true)
    public User login(@NotBlank String account, @NotBlank String password) throws BusinessException {
        Long accountId = accountFacade.loadBy(account, password);
        Optional<User> option = this.jpaSpecificationExecutor.findOne((root, query, cb) -> cb.equal(root.get("accountId"), accountId));
        option.orElseThrow(() -> new BusinessException("用户不存在,登录失败"));
        return option.get();
    }


}
