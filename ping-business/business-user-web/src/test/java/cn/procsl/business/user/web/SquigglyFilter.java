package cn.procsl.business.user.web;

import cn.procsl.business.user.web.controller.FilterController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.bohnman.squiggly.Squiggly;
import com.github.bohnman.squiggly.util.SquigglyUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

/**
 * @author procsl
 * @date 2020/02/07
 */
@Slf4j
public class SquigglyFilter {


    FilterController filterController;

    @Before
    public void before() {
        filterController = new FilterController();
    }


    @Test
    public void test() {
        ObjectMapper objectMapper = Squiggly.init(new XmlMapper(), "three.*");
        String output = SquigglyUtils.stringify(objectMapper, filterController.api());
        log.debug(output);
    }
}
