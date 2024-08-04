package cn.procsl.ping.boot.jpa.support.query.ast;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class SimpleFromClauseTest {

    @Test
    public void toClauseString() {

        FragmentClause fc = new FragmentClause("db.t_user");
        SimpleFromClause s = new SimpleFromClause(fc, "u");
        log.info(s.toClauseString());
        Assertions.assertEquals("db.t_user as u", s.toClauseString());
    }
}
