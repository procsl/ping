package cn.procsl.ping.boot.admin.web;

import lombok.val;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class LoginUtils {


    /**
     * 登录
     */
    public static MockHttpSession toLogin(MockMvc mockMvc) throws Exception {
        val httpSession = new MockHttpSession();
        MockHttpServletRequestBuilder processor = post("/v1/auth")
                .session(httpSession)
                .param("username", "admin")
                .param("password", "123456")
                .contentType(APPLICATION_FORM_URLENCODED);
        mockMvc.perform(processor).andExpect(status().is3xxRedirection());
        return httpSession;
    }

}
