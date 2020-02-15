package cn.procsl.ping.business.user.service;

import cn.procsl.ping.business.exception.*;
import cn.procsl.ping.business.query.Page;
import cn.procsl.ping.business.query.QueryContext;
import cn.procsl.ping.business.user.dto.UserDTO;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户服务
 * 包含用户相关的服务接口
 *
 * @author procsl
 * @date 2019/12/11
 */
public interface UserService {

    /**
     * 创建用户
     * 可以批量创建用户
     * 若出现业务异常,实现者可以不保证该注册是原子性的(即允许部分失败部分成功,但是需要抛出异常)
     *
     * @param user 用户信息列表
     * @return 返回创建成功条数
     * @throws IllegalArgsException 若参数不合法则抛出异常
     * @throws EntityExistException 若用户账户/邮箱/手机号已存在则抛出此异常
     * @throws BusinessException    通用的业务异常
     */
    Integer create(@NotNull @NotEmpty @Size(min = 1, max = 10) @Valid List<@NotNull UserDTO> user) throws IllegalArgsException, EntityExistException, BusinessException;

    /**
     * 更新用户信息
     *
     * @param user 用户信息实体
     * @return 返回成功修改的用户的信息的条数
     * @throws IllegalArgsException    如果更新出现错误则抛出异常
     * @throws EntityNotFoundException 实体未找到异常
     * @throws BusinessException       通用的业务异常
     */
    Integer update(@NotNull @NotEmpty @Size(min = 1, max = 10) List<@NotNull @NotEmpty @Valid UserDTO> user) throws IllegalArgsException, EntityNotFoundException, BusinessException;

    /**
     * 账户查询方法
     *
     * @param queryContext 账户的查询条件
     * @return 返回查询到的数据
     * @throws UnSupportQueryException 查询上下文错误异常/不支持的查询
     */
    Page<UserDTO> query(@NotNull QueryContext<UserDTO> queryContext) throws UnSupportQueryException;

    /**
     * 删除用户信息
     *
     * @param id 用户ID
     * @return 返回成功删除的用户信息条数
     * @throws BusinessException 如果删除出现问题则抛出异常
     */
    Integer delete(@NotNull @NotEmpty @Size(min = 1, max = 10) List<@NotNull @NotEmpty @Size(min = 32, max = 32) String> id) throws BusinessException;
}
