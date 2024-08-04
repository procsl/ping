package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class SimpleSelectClauseTest {


    @Test
    public void toClauseString() {

        SimpleSelectClause simple = new SimpleSelectClause(new FragmentClause("tb"), "name", String.class, 0);
        log.info(simple.toString());
        Assertions.assertEquals("tb as name", simple.toClauseString());

    }

}
