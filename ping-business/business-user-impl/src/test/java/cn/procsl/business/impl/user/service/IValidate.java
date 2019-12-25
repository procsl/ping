package cn.procsl.business.impl.user.service;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author procsl
 * @date 2019/12/20
 */
public interface IValidate {


    void testNull(@NotNull Object user);

    void testEmpty(@NotEmpty String obj);

    void testSize(@Size(min = 1, max = 2) List<Integer> list);

}
