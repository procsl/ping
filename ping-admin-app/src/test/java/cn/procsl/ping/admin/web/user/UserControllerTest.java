package cn.procsl.ping.admin.web.user;


import cn.procsl.ping.admin.AdminApplication;
import cn.procsl.ping.boot.infra.domain.user.Gender;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.javafaker.Faker;
import com.github.jsonzou.jmockdata.MockConfig;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicLong;

import static com.github.jsonzou.jmockdata.JMockData.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = AdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class UserControllerTest {


    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserController userController;

    JsonMapper jsonMapper = new JsonMapper();
    AtomicLong gid = new AtomicLong();

    @BeforeEach
    public void beforeEach() throws Exception {
        Assertions.assertNotNull(userController);
        MockConfig config = new MockConfig().stringRegex("[a-zA-Z0-9_]{5,20}");
        String account = mock(String.class, config);
        RegisterDTO user = new RegisterDTO(account, "password");
        mockMvc.perform(
                        post("/v1/users")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(user))
                )
                .andDo(result -> {
                    String str = result.getResponse().getContentAsString();
                    gid.set(Long.parseLong(str));
                });
        log.info("BeforeEach is end!");
    }

    @Test
    public void register() throws Exception {
        RegisterDTO user = new RegisterDTO("program_chen@foxmail.com", "password");
        mockMvc.perform(
                        post("/v1/users")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(user))
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andDo(result -> {
                    String str = result.getResponse().getContentAsString();
                    Assertions.assertNotNull(str);
                });

    }

    @Test
    public void update() throws Exception {
        UserPropDTO prop = new UserPropDTO(Faker.instance().name().username(), Gender.man, "这是备注");
        val builder =
                patch("/v1/users/{id}", gid.get())
                        .content(jsonMapper.writeValueAsString(prop))
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON);

        mockMvc.perform(builder)
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void findUsers() throws Exception {
        val builder =
                get("/v1/users")
                        .param("limit", "1")
                        .param("sort", "id", "desc")
                        .accept(APPLICATION_JSON);

        mockMvc.perform(builder).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].id").isNotEmpty())
                .andExpect(jsonPath("$.content[*].name").isNotEmpty())
                .andExpect(jsonPath("$.content[*].gender").isNotEmpty())
                .andExpect(jsonPath("$.content[*].remark").isNotEmpty())
                .andExpect(jsonPath("$.content[*].account").isNotEmpty())
                .andExpect(jsonPath("$.content[*].account.name").isNotEmpty())
                .andExpect(jsonPath("$.content[*].account.state").isNotEmpty())
                .andExpect(jsonPath("$.limit").value("1"))
                .andExpect(jsonPath("$.empty").value("false"))
                .andDo(print());
    }
}