package cn.procsl.business.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author procsl
 * @date 2019/12/20
 */
@Component
@Validated
@Slf4j
public class ValidateTest implements IValidate {

    @Override
    public void testNull(@NotNull Object user) {
        throw new RuntimeException("失败");
    }

    @Override
    public void testEmpty(@NotEmpty String obj) {
        throw new RuntimeException("失败");
    }

    @Override
    public void testSize(@Size(min = 1, max = 2) List<Integer> list) {
        throw new RuntimeException("失败");
    }
}
