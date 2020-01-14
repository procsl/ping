package cn.procsl.business.user.web;

import cn.procsl.business.user.web.component.filter.LogTraceFilter;
import cn.procsl.business.user.web.controller.GeneralController;
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
 * @date 2020/01/09
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/user-web.xml")
@WebAppConfiguration
public class GeneralTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    MockMvc mockMvc;

    @Autowired
    GeneralController controller;

    @Before
    public void before() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(new LogTraceFilter()).build();
        Assert.assertNotNull("mockMvc 初始化失败", mockMvc);
    }

    @Test
    public void formatJsonMap() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/format/map.json");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key1").value("value"))
                .andExpect(jsonPath("$.key2").value("value2"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    public void formatJsonList() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/format/list.json");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("hello"))
                .andExpect(jsonPath("$[1]").value("world"))
                .andExpect(jsonPath("$[2]").value("list"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }


    @Test
    public void formatXmlMap() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/format/map.xml");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(xpath("/root/key1").string("value"))
                .andExpect(xpath("/root/key2").string("value2"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    @Test
    public void formatXmlList() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/format/list.xml");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(xpath("/root/item[1]").string("hello"))
                .andExpect(xpath("/root/item[2]").string("world"))
                .andExpect(xpath("/root/item[3]").string("list"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }

    @Test
    public void formatXmlObject() throws Exception {
        MockHttpServletRequestBuilder get = MockMvcRequestBuilders.get("/format/object.xml");
        mockMvc.perform(get)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
    }
}
