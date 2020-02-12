package cn.procsl.business.user.web;

import cn.procsl.business.user.web.controller.FilterController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.github.shihyuho.jackson.databind.DynamicFilterMixIn;
import com.github.shihyuho.jackson.databind.DynamicFilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

/**
 * @author procsl
 * @date 2020/02/07
 */
@Slf4j
public class JacksonDynamicFilter {


    FilterController filterController;

    @Before
    public void before() {
        filterController = new FilterController();
    }


    @Test
    public void test() throws JsonProcessingException {

        ObjectMapper mapper = new JsonMapper();
        mapper.addMixIn(Object.class, DynamicFilterMixIn.class);
        mapper.setFilterProvider(new DynamicFilterProvider());

        PropertyFilter someFilter = SimpleBeanPropertyFilter.filterOutAllExcept("three.*");

        String jsonWithoutSomeField = mapper
                .writer(new DynamicFilterProvider(someFilter)) // determine custom filter
                .writeValueAsString(filterController.api());

        log.debug(jsonWithoutSomeField);
    }
}
