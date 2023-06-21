package cn.procsl.ping.boot.web.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class JsonFilterTest {

    ObjectMapper jsonMapper = new ObjectMapper();


    @Test
    public void test() {
        Configuration config = Configuration.builder().jsonProvider(new JacksonJsonProvider(jsonMapper)).mappingProvider(new JacksonMappingProvider()).build();
        ParseContext context = JsonPath.using(config);
        DocumentContext document = context.parse("{\"id\": \"123456\"}");
    }

}
