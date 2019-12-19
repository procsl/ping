package cn.procsl.business.user.service;

import cn.procsl.business.exception.*;
import cn.procsl.business.query.Page;
import cn.procsl.business.query.QueryContext;
import cn.procsl.business.user.domain.UserDO;
import cn.procsl.business.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 用户服务实现类
 *
 * @author procsl
 * @date 2019/12/12
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer create(@NotNull @NotEmpty @Size(min = 1) UserDTO... users) throws EntityExistException, BusinessException {
        for (UserDTO dto : users) {
            new UserDO(dto).save();
        }
        return users.length;
    }

    @Override
    public Integer update(@NotNull @NotEmpty @Size(min = 1) UserDTO... users) throws EntityNotFoundException, BusinessException {
        for (UserDTO dto : users) {
            new UserDO(dto).update();
        }
        return users.length;
    }

    @Override
    public Page<UserDTO> query(@NotNull QueryContext<UserDTO> queryContext) throws UnSupportQueryException {
        return null;
    }

    @Override
    public Integer delete(@NotNull @NotEmpty @Size(min = 1) String... ids) throws BusinessException {
        for (String id : ids) {
            new UserDO(id).delete();
        }
        return ids.length;
    }
}
