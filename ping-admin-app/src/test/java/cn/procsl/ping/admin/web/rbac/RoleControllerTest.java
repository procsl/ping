package cn.procsl.ping.admin.web.rbac;

import cn.procsl.ping.admin.AdminApplication;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.github.jsonzou.jmockdata.JMockData.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = AdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class RoleControllerTest {

    @Autowired
    MockMvc mockMvc;

    JsonMapper jsonMapper = new JsonMapper();
    AtomicLong gid = new AtomicLong();

    @BeforeEach
    public void beforeEach() throws Exception {
        RoleDetailsDTO role = new RoleDetailsDTO(Faker.instance().name().fullName(), Arrays.stream(mock(String[].class)).collect(Collectors.toList()));
        mockMvc.perform(
                        post("/v1/roles")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(role))
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andDo(result -> {
                    String str = result.getResponse().getContentAsString();
                    gid.set(Long.parseLong(str));
                });
        log.info("BeforeEach is end!");
    }


    @Test
    void create() throws Exception {
        RoleDetailsDTO role = new RoleDetailsDTO(Faker.instance().name().fullName(), Arrays.stream(mock(String[].class)).collect(Collectors.toList()));
        mockMvc.perform(
                        post("/v1/roles")
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(role))
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andDo(result -> {
                    String str = result.getResponse().getContentAsString();
                    log.info("id:{}", str);
                });
    }

    @Test
    void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/roles/{id}", gid.get()))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void change() throws Exception {
        RoleDetailsDTO role = new RoleDetailsDTO(Faker.instance().name().fullName(), null);
        mockMvc.perform(
                        patch("/v1/roles/{id}", gid.get())
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(role))
                )
                .andExpect(status().is2xxSuccessful());

        role.setPermissions(Arrays.stream(mock(String[].class)).collect(Collectors.toList()));
        role.setName(null);
        mockMvc.perform(
                        patch("/v1/roles/{id}", gid.get())
                                .contentType(APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsString(role))
                )
                .andExpect(status().is2xxSuccessful());

    }

    @Test
    void getById() throws Exception {
        mockMvc.perform(get("/v1/roles/{id}", gid.get()).accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.name").isNotEmpty())
                .andExpect(jsonPath("$.permissions").isArray());

    }


    @Test
    void findRoles() throws Exception {
        val builder =
                get("/v1/roles")
                        .param("limit", "1")
                        .param("sort", "id", "desc")
                        .accept(APPLICATION_JSON);

        mockMvc.perform(builder).andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isNotEmpty())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.limit").value("1"))
                .andExpect(jsonPath("$.empty").value("false"))
                .andDo(print());
    }
}