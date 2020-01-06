package cn.procsl.business.user.web;

import cn.procsl.business.user.web.component.DispatcherServlet;
import cn.procsl.business.user.web.controller.AcceptedTestController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * @author procsl
 * @date 2020/01/06
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestContext.class}, locations = "classpath:*spring/user-web.xml")
@WebAppConfiguration
public class AcceptedTest {

    @Autowired
    private AcceptedTestController acceptedTestController;
//
//    @Before
//    public void before() {
//        DispatcherServlet servlet = new DispatcherServlet();
//        servlet.setThrowExceptionIfNoHandlerFound(true);
//        servlet.setContextConfigLocation("classpath:spring/user-web.xml");
//        mockMvc = new MockMvc(servlet);
//    }


    @Test
    public void testString() {
//        MockMvcRequestBuilders.post("")
    }

}
