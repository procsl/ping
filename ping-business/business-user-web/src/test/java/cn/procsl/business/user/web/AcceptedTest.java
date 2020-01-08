package cn.procsl.business.user.web;

import cn.procsl.business.user.web.component.DispatcherServlet;
import cn.procsl.business.user.web.controller.AcceptedTestController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author procsl
 * @date 2020/01/06
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/user-web.xml")
@WebAppConfiguration
public class AcceptedTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    MockMvc mockMvc;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Assert.assertNotNull("mockMvc 初始化失败", mockMvc);
    }


    @Test
    public void testString() throws Exception {
        MockHttpServletRequestBuilder post = post("/accepted/string");
        mockMvc.perform(post)
                // 必须是Accepted的状态码
                .andExpect(status().isAccepted())
                .andExpect(content().string("string"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    public void testDouble() throws Exception {
        MockHttpServletRequestBuilder post = post("/accepted/double");
        mockMvc.perform(post)
                // 必须是Accepted的状态码
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isAccepted());
    }

    @Test
    public void testLong() throws Exception {
        MockHttpServletRequestBuilder post = post("/accepted/long");
        mockMvc.perform(post)
                // 必须是Accepted的状态码
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(content().string("3000"));
    }

    @Test
    public void testInteger() throws Exception {
        MockHttpServletRequestBuilder post = post("/accepted/integer");
        mockMvc.perform(post)
                // 必须是Accepted的状态码
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isAccepted())
                .andExpect(content().string("100"));
    }

    @Test
    public void testDate() throws Exception {
        MockHttpServletRequestBuilder post = post("/accepted/date");
        mockMvc.perform(post)
                // 必须是Accepted的状态码
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isAccepted());
    }

}
