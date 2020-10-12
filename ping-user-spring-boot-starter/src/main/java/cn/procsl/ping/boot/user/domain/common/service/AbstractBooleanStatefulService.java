package cn.procsl.ping.boot.user.domain.common.service;

import cn.procsl.ping.boot.domain.business.state.model.BooleanStateful;
import cn.procsl.ping.boot.domain.business.state.repository.BooleanStatefulRepository;
import lombok.NonNull;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public interface AbstractBooleanStatefulService<T extends BooleanStateful<ID>, ID extends Serializable> {


    /**
     * 禁用指定的实体
     *
     * @param id
     */
    default int disable(@NotNull ID id) {
        return getBooleanStatefulRepository().disable(id);
    }

    /**
     * 启用指定的实体
     *
     * @param id
     */
    default int enable(@NotNull ID id) {
        return getBooleanStatefulRepository().enable(id);
    }

    /**
     * 修改状态
     *
     * @param id    指定的ID
     * @param state 状态
     */
    default int changeState(@NotNull ID id, @NotNull Boolean state) {
        return getBooleanStatefulRepository().changeState(id, state);
    }

    /**
     * 获取指定实体的状态
     *
     * @param id 指定的实体
     * @return 返回实体状态
     */
    default Boolean getState(@NonNull ID id) {
        return getBooleanStatefulRepository().getState(id);
    }

    BooleanStatefulRepository<T, ID> getBooleanStatefulRepository();
}
