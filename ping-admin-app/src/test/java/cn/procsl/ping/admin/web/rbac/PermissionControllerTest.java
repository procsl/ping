package cn.procsl.ping.admin.web.rbac;

import cn.procsl.ping.admin.AdminApplication;
import cn.procsl.ping.admin.web.LoginUtils;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.jsonzou.jmockdata.JMockData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = AdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PermissionControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    MockMvc mockMvc;

    JsonMapper jsonMapper = new JsonMapper();

    AtomicLong gid = new AtomicLong();

    @BeforeEach
    public void setUp() throws Exception {
    }

    @Test
    public void create() throws Exception {

        val permission = JMockData.mock(PermissionCreateDTO.class);
        permission.setType(PermissionType.page);

        mockMvc.perform(
                        post("/v1/permissions")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(permission))
                                .session(LoginUtils.toLogin(mockMvc))
                )
                .andExpect(status().is2xxSuccessful())
                .andDo(result -> {
                    String str = result.getResponse().getContentAsString();
                    gid.set(Long.parseLong(str));
                });

    }

    @Test
    public void delete() {
    }

    @Test
    public void update() {
    }

    @Test
    public void findPermissions() {
    }
}