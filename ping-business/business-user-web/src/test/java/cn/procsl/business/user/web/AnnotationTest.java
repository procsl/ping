package cn.procsl.business.user.web;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author procsl
 * @date 2020/01/08
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/user-web.xml")
@WebAppConfiguration
public class AnnotationTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    MockMvc mockMvc;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        Assert.assertNotNull("mockMvc 初始化失败", mockMvc);
    }

    @Test
    public void postAccept() throws Exception {
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/accepted");
        mockMvc.perform(post)
                .andExpect(status().isAccepted())
                .andExpect(content().string("accepted"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }


    @Test
    public void get() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/accepted");
        mockMvc.perform(get)
                .andExpect(status().isOk())
                .andExpect(content().string("accepted"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    public void postNoContent() throws Exception {
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/noContent");
        mockMvc.perform(post).andExpect(status().isNoContent());
    }

    @Test
    public void postNoContent2() throws Exception {
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/noContent2");
        mockMvc.perform(post).andExpect(status().isNoContent());
    }


    @Test
    public void postCreate() throws Exception {
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/create");
        mockMvc.perform(post)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    public void postCreate2() throws Exception {
        MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post("/create2");
        mockMvc.perform(post)
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN_VALUE));
    }
}
