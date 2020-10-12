package cn.procsl.ping.boot.user.domain.user.service;

import cn.procsl.ping.boot.domain.business.state.repository.BooleanStatefulRepository;
import cn.procsl.ping.boot.domain.business.utils.StringUtils;
import cn.procsl.ping.boot.user.domain.common.service.AbstractBooleanStatefulService;
import cn.procsl.ping.boot.user.domain.common.service.AbstractService;
import cn.procsl.ping.boot.user.domain.user.model.Account;
import cn.procsl.ping.boot.user.domain.user.model.QAccount;
import cn.procsl.ping.boot.user.domain.user.model.User;
import cn.procsl.ping.business.exception.BusinessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Validated
@Transactional(rollbackFor = Exception.class)
public class UserService extends AbstractService<String, User> implements AbstractBooleanStatefulService<User, String> {

    final QuerydslPredicateExecutor<Account> accountRepo;

    final JpaRepository<Account, Long> jpaAccountRepository;

    final BooleanStatefulRepository<User, String> booleanStatefulRepository;

    @Inject
    public UserService(JpaRepository<User, String> jpaRepository,
                       QuerydslPredicateExecutor<User> querydslRepository,
                       JpaRepository<Account, Long> jpaAccountRepository,
                       QuerydslPredicateExecutor<Account> accountRepo,
                       BooleanStatefulRepository<User, String> booleanStatefulRepository

    ) {
        super(jpaRepository, querydslRepository);
        this.accountRepo = accountRepo;
        this.jpaAccountRepository = jpaAccountRepository;
        this.booleanStatefulRepository = booleanStatefulRepository;
    }


    /**
     * 创建用户
     *
     * @param realName  用户姓名
     * @param account   账户
     * @param password  密码
     * @param createdBy 创建人
     * @return 返回创建成功后的用户名
     * @throws BusinessException 如果创建失败, 抛出此异常
     */
    @Transactional
    public String register(String realName,
                           @NotEmpty @Size(min = Account.ACCOUNT_MIN, max = Account.ACCOUNT_MAX) String account,
                           @NotEmpty @Size(min = Account.PASSWORD_LEN, max = Account.PASSWORD_LEN) String password,
                           String createdBy) throws BusinessException {
        if (StringUtils.isEmpty(realName)) {
            realName = account;
        }

        // TODO
        User newUser = this.jpaRepository.save(new User(realName, account, password, createdBy));
        this.jpaAccountRepository.save(newUser.getAccount());
        return newUser.getId();
    }


    /**
     * 用户登录
     *
     * @param account  账户
     * @param password 用户密码
     * @throws BusinessException 如果登录失败
     */
    @Transactional(readOnly = true)
    public User login(String account, String password) throws BusinessException {
        Account acc = this.accountRepo.findOne(QAccount.account.name.eq(account)).orElseThrow(() -> new BusinessException("用户不存在"));
        if (acc == null) {
            throw new BusinessException("用户不存在");
        }
        User user = acc.getUser();
        if (!user.isEnable()) {
            throw new BusinessException("用户已被禁用");
        }
        acc.validatePassword(password);
        return user;
    }


    /**
     * 密码重置
     *
     * @param userId   指定的用户
     * @param password 新密码
     * @throws BusinessException 如果重置失败
     */
    @Transactional
    public void resetPassword(@NotEmpty @Size(min = 32, max = 32) String userId,
                              @NotEmpty @Size(min = Account.PASSWORD_LEN, max = Account.PASSWORD_LEN) String password) throws

        BusinessException {
        User user = this.getOne(userId);
        user.resetPassword(password);
        this.jpaRepository.save(user);
    }

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param password    新密码
     */
    @Transactional
    public void changePassword(@NotEmpty @Size(min = 32, max = 32) String userId,
                               @NotEmpty @Size(min = Account.PASSWORD_LEN, max = Account.PASSWORD_LEN)
                                   String oldPassword,
                               @NotEmpty @Size(min = Account.PASSWORD_LEN, max = Account.PASSWORD_LEN)
                                   String password) {
        User user = this.getOne(userId);
        user.changePassword(oldPassword, password);
        this.jpaRepository.save(user);
    }

    @Override
    public BooleanStatefulRepository<User, String> getBooleanStatefulRepository() {
        return booleanStatefulRepository;
    }
}
