package cn.procsl.ping.web.component.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * @author procsl
 * @date 2020/02/14
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-web.xml")
@WebAppConfiguration
public class LogTraceFilterTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new LogTraceFilter()).build();
        Assert.assertNotNull("mockMvc 初始化失败", mockMvc);
    }

    @Test
    public void trace() throws Exception {
        MockHttpServletRequestBuilder get = get("/404");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(header().exists("X-trace-id"));
    }


}
