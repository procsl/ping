package cn.procsl.ping.boot.admin.web;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public final class LoginUtils {


    /**
     * 登录
     */
    public static MockHttpSession toLogin(MockMvc mockMvc) throws Exception {
        val httpSession = new MockHttpSession();
        JSONObject json = new JSONObject();
        json.put("account", "admin");
        json.put("password", "123456");
        log.info("开始登录");
        MockHttpServletRequestBuilder processor = post("/v1/session")
                .session(httpSession)
                .content(json.toString())
                .contentType(APPLICATION_JSON);
        mockMvc.perform(processor).andExpect(status().is2xxSuccessful());
        log.info("登录成功");
        return httpSession;
    }

}
