package cn.procsl.ping.web.component.converter;


import cn.procsl.ping.web.component.filter.LogTraceFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * @author procsl
 * @date 2020/02/14
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-web.xml")
@WebAppConfiguration
public class NumberHttpMessageConverterTest {

    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(new LogTraceFilter()).build();
        Assert.assertNotNull("mockMvc 初始化失败", mockMvc);
    }


    @Test
    public void longType() throws Exception {
        MockHttpServletRequestBuilder post = get("/number/long");
        mockMvc.perform(post)
                .andDo(print())
                .andExpect(content().string("10"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    public void IntegerType() throws Exception {
        MockHttpServletRequestBuilder post = get("/number/integer");
        mockMvc.perform(post)
                .andDo(print())
                .andExpect(content().string("10"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    public void shortType() throws Exception {
        MockHttpServletRequestBuilder post = get("/number/short");
        mockMvc.perform(post)
                .andDo(print())
                .andExpect(content().string("10"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    public void doubleType() throws Exception {
        MockHttpServletRequestBuilder post = get("/number/double");
        mockMvc.perform(post)
                .andDo(print())
                .andExpect(content().string("10.0"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    public void floatType() throws Exception {
        MockHttpServletRequestBuilder post = get("/number/float");
        mockMvc.perform(post)
                .andDo(print())
                .andExpect(content().string("10.0"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    public void byteType() throws Exception {
        MockHttpServletRequestBuilder post = get("/number/byte");
        mockMvc.perform(post)
                .andDo(print())
                .andExpect(content().string("1"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }
}
