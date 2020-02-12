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
 * @date 2020/02/07
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/user-web.xml")
@WebAppConfiguration
public class JsonFilterTest {

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
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/apis.json");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.one").exists())
                .andExpect(jsonPath("$.two").exists())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void defaultFilter() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/apis.json?field=two");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.two").exists())
                .andExpect(jsonPath("$.one").doesNotExist())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void excludeFilter() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/apis.json?field=two&pattern=exclude");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.two").doesNotExist())
                .andExpect(jsonPath("$.one").exists())
                .andExpect(jsonPath("$.three").exists())
                .andExpect(jsonPath("$.stack").exists())
                .andExpect(jsonPath("$.stacks").exists())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void includeFilter() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/apis.json?field=two&pattern=include");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.two").exists())
                .andExpect(jsonPath("$.one").doesNotExist())
                .andExpect(jsonPath("$.three").doesNotExist())
                .andExpect(jsonPath("$.stack").doesNotExist())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void includeFilterMore() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/apis.json?field=two,one,stacks.name&pattern=include");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.two").exists())
                .andExpect(jsonPath("$.one").exists())
                .andExpect(jsonPath("$.stack").doesNotExist())
                .andExpect(jsonPath("$.stacks.*.name").exists())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void excludeFilterMore() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/filter/apis.json?field=two,one,stacks.name,stacks.fuck,stack.echo,stack.fuck,three.list.name,three.array&pattern=exclude");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.two").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.one").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.stack").exists())
                .andExpect(jsonPath("$.stacks.*.name").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.stacks.*.echo").exists())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

}
