//package cn.procsl.ping.boot.jpa.support.query.repository.ext;
//
//import cn.procsl.ping.boot.jpa.support.query.RequestParamDTO;
//import cn.procsl.ping.boot.jpa.support.query.repository.BuilderContext;
//import cn.procsl.ping.boot.jpa.support.query.repository.PojoSearchObjectBuilder;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//
//import java.util.Date;
//
//@Slf4j
//public class PojoSearchObjectBuilderTest {
//
//    private PojoSearchObjectBuilder builder;
//
//    @Test
//    public void select() {
//        RequestParamDTO dto = new RequestParamDTO();
//        dto.setName("你好");
//        dto.setStartDate(new Date());
//        builder = new PojoSearchObjectBuilder(dto);
//        BuilderContext context = new BuilderContext() {
//            @Override
//            public boolean isFormat() {
//                return true;
//            }
//        };
//        String hql = builder.toClauseString(context);
//        log.info("查询: {}", hql);
//    }
//
//}
