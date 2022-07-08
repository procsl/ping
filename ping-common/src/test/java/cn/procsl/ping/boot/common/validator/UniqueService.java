package cn.procsl.ping.boot.common.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Indexed;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Indexed
@Slf4j
@Validated
public class UniqueService {

    public void create(@UniqueField(entity = Unique.class, fieldName = "column") String name) {
        throw new UnsupportedOperationException("测试异常");
    }

    public void create(@UniqueField(entity = Unique.class, fieldName = "column") Unique unique) {
        throw new UnsupportedOperationException("测试异常");
    }

    public void create(@UniqueField(entity = Unique.class, fieldName = "column") UniqueDTO unique) {
        throw new UnsupportedOperationException("测试异常");
    }

}
