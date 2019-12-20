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

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户服务实现类
 * 参数校验, 仅支持放在类上 且只能为 @Validated
 *
 * @author procsl
 * @date 2019/12/12
 */
@Slf4j
@Service
@Validated
public class UserServiceImpl implements UserService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer create(List<UserDTO> users) throws EntityExistException, BusinessException {
        for (UserDTO dto : users) {
            new UserDO(dto).save();
        }
        return users.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer update(List<UserDTO> users) throws EntityNotFoundException, BusinessException {
        for (UserDTO dto : users) {
            new UserDO(dto).update();
        }
        return users.size();
    }

    @Override
    public Page<UserDTO> query(QueryContext<UserDTO> queryContext) throws UnSupportQueryException {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer delete(List<String> ids) throws BusinessException {
        for (String id : ids) {
            new UserDO(id).delete();
        }
        return ids.size();
    }
}
