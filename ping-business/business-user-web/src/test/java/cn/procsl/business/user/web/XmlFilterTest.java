package cn.procsl.business.user.web;

import cn.procsl.business.user.web.component.filter.LogTraceFilter;
import cn.procsl.business.user.web.controller.FilterController;
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

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author procsl
 * @date 2020/02/11
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/user-web.xml")
@WebAppConfiguration
public class XmlFilterTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    MockMvc mockMvc;

    @Autowired
    FilterController controller;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(new LogTraceFilter()).build();
        Assert.assertNotNull("mockMvc 初始化失败", mockMvc);
    }

    @Test
    public void notFilter() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/apis.xml");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }


    @Test
    public void defaultFilter() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/apis.xml?field=two");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(xpath("/root/two").exists())
                .andExpect(xpath("/root/one").doesNotExist())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    @Test
    public void excludeFilter() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/apis.xml?field=two&pattern=exclude");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(xpath("/root/two").doesNotExist())
                .andExpect(xpath("/root/@one").exists())
                .andExpect(xpath("/root/three").exists())
                .andExpect(xpath("/root/stack").exists())
                .andExpect(xpath("/root/stacks").exists())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    @Test
    public void excludeFilterMore() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/apis.xml?field=two,stacks.name&pattern=exclude");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(xpath("/root/two").doesNotExist())
                .andExpect(xpath("/root/@one").exists())
                .andExpect(xpath("/root/three").exists())
                .andExpect(xpath("/root/stack").exists())
                .andExpect(xpath("/root/stacks/name").doesNotExist())
                .andExpect(xpath("/root/stacks/echo").exists())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    @Test
    public void skipFilter() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/skip-filter.xml?field=message&pattern=exclude");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(xpath("/root/two").exists())
                .andExpect(xpath("/root/@one").exists())
                .andExpect(xpath("/root/three").exists())
                .andExpect(xpath("/root/stack").exists())
                .andExpect(xpath("/root/stacks/name").exists())
                .andExpect(xpath("/root/stacks/echo").exists())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

}
