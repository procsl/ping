package cn.procsl.ping.boot.jpa.support.query.repository.ext;

import cn.procsl.ping.boot.jpa.support.query.RequestParamDTO;
import cn.procsl.ping.boot.jpa.support.query.repository.BuilderContext;
import cn.procsl.ping.boot.jpa.support.query.repository.PojoSearchObjectBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class PojoSearchObjectBuilderTest {

    private final PojoSearchObjectBuilder builder = new PojoSearchObjectBuilder(new RequestParamDTO());

    @Test
    public void select() {
        BuilderContext context = new BuilderContext() {
            @Override
            public boolean isFormat() {
                return true;
            }
        };
        String hql = builder.toClauseString(context);
        log.info("查询: {}", hql);
    }

}
