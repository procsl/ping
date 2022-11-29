package cn.procsl.ping.boot.system.web.config;

import cn.procsl.ping.boot.system.TestSystemApplication;
import cn.procsl.ping.boot.system.web.LoginUtils;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ConfigControllerTest {

    @Inject
    MockMvc mockMvc;

    JsonMapper jsonMapper = new JsonMapper();

    AtomicLong gid = new AtomicLong();
    MockHttpSession session;

    @BeforeEach
    public void setUp() throws Exception {

        this.session = LoginUtils.toLogin(mockMvc);

        val config = new ConfigDTO("key", "test1", "desc");
        mockMvc.perform(
                       MockMvcRequestBuilders.put("/v1/configs")
                                             .contentType(APPLICATION_JSON)
                                             .content(jsonMapper.writeValueAsString(config))
                                             .session(session)
               )
               .andExpect(status().is2xxSuccessful())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andDo(result -> {
                   String str = result.getResponse().getContentAsString();
                   Assertions.assertNotNull(str);
                   gid.set(Long.parseLong(str));
               });
    }

    @Test
    public void edit() throws Exception {

        val config = new ConfigDTO("key2", "test", "desc");
        mockMvc.perform(
                       MockMvcRequestBuilders.patch("/v1/configs/{id}", gid.get())
                                             .contentType(APPLICATION_JSON)
                                             .content(jsonMapper.writeValueAsString(config))
                                             .session(session)
               )
               .andExpect(status().is2xxSuccessful());

    }

    @Test
    public void put() throws Exception {
        val config = new ConfigDTO("key3", "test", "desc");
        mockMvc.perform(
                       MockMvcRequestBuilders.put("/v1/configs")
                                             .contentType(APPLICATION_JSON)
                                             .content(jsonMapper.writeValueAsString(config))
                                             .session(session)
               )
               .andExpect(status().is2xxSuccessful())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andDo(result -> {
                   String str = result.getResponse().getContentAsString();
                   Assertions.assertNotNull(str);
               });
    }

    @Test
    public void delete() throws Exception {
        mockMvc.perform(
                       MockMvcRequestBuilders.delete("/v1/configs/{id}", gid.get())
                                             .session(session)
               )
               .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getConfig() throws Exception {
        mockMvc.perform(
                       MockMvcRequestBuilders.get("/v1/configs/{key}", "key")
                                             .session(session)
               )
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content").value("test1"));
    }

    @Test
    public void findConfig() throws Exception {
        mockMvc.perform(
                       MockMvcRequestBuilders.get("/v1/configs")
                                             .session(session)
               )
               .andExpect(status().isOk())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andExpect(jsonPath("$.content").isNotEmpty())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content[*].id").isNotEmpty())
               .andExpect(jsonPath("$.content[*].key").isNotEmpty())
               .andExpect(jsonPath("$.content[*].description").isNotEmpty())
               .andExpect(jsonPath("$.content[*].content").isNotEmpty())
               .andExpect(jsonPath("$.empty").value("false"));
    }
}