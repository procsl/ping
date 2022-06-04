package cn.procsl.ping.admin.web.setting;

import cn.procsl.ping.admin.AdminApplication;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.jsonzou.jmockdata.JMockData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = AdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class SettingControllerTest {

    @Autowired
    MockMvc mockMvc;

    JsonMapper jsonMapper = new JsonMapper();

    @BeforeEach
    public void setUp() throws Exception {
        val data = JMockData.mock(String[].class);
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/v1/setting/default-roles")
                        .contentType(APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(data))
        );
    }

    @Test
    public void defaultRoleSetting() throws Exception {
        val data = JMockData.mock(String[].class);
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/v1/setting/default-roles")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(data))
                )
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getDefaultRoles() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/setting/default-roles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*]").isNotEmpty());

    }
}