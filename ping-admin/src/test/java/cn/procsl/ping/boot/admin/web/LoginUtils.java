package cn.procsl.ping.boot.admin.web;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public final class LoginUtils {


    /**
     * 登录
     */
    public static MockHttpSession toLogin(MockMvc mockMvc) throws Exception {
        val httpSession = new MockHttpSession();
        log.info("开始登录");
        MockHttpServletRequestBuilder processor = post("/v1/auth")
                .session(httpSession)
                .param("username", "admin")
                .param("password", "123456")
                .contentType(APPLICATION_FORM_URLENCODED);
        mockMvc.perform(processor).andExpect(status().is3xxRedirection());
        log.info("登录成功");
        return httpSession;
    }

}
