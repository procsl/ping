package cn.procsl.business.user.web;

import cn.procsl.business.user.web.component.filter.LogTraceFilter;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * @author procsl
 * @date 2020/01/06
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/user-web.xml")
@WebAppConfiguration
public class SimpleTypeTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    MockMvc mockMvc;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(new LogTraceFilter()).build();
        Assert.assertNotNull("mockMvc 初始化失败", mockMvc);
    }


    @Test
    public void testString() throws Exception {
        MockHttpServletRequestBuilder post = post("/simple/string");
        mockMvc.perform(post)
                .andExpect(content().string("string"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    public void testDouble() throws Exception {
        MockHttpServletRequestBuilder post = post("/simple/double");
        mockMvc.perform(post)
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    public void testLong() throws Exception {
        MockHttpServletRequestBuilder post = post("/simple/long");
        mockMvc.perform(post)
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(content().string("3000"));
    }

    @Test
    public void testInteger() throws Exception {
        MockHttpServletRequestBuilder post = post("/simple/integer");
        mockMvc.perform(post)
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(content().string("100"));
    }

    @Test
    public void testDate() throws Exception {
        MockHttpServletRequestBuilder post = post("/simple/date");
        mockMvc.perform(post)
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

}
