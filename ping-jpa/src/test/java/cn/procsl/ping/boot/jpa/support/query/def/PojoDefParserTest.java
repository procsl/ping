//package cn.procsl.ping.boot.jpa.support.query.def;
//
//import cn.procsl.ping.boot.jpa.support.query.ProjectionDTO;
//import cn.procsl.ping.boot.jpa.support.query.builder.def.PojoDefParser;
//import com.fasterxml.jackson.core.JsonParser;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.PropertyNamingStrategy;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//
//@Slf4j
//public class PojoDefParserTest {
//
//
//    @Test
//    public void test() throws JsonProcessingException {
//        PojoDefParser parser = new PojoDefParser(ProjectionDTO.class);
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
//        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
//        String string = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parser);
//        log.info("\n----\n{}\n-----", string);
//    }
//
//}
