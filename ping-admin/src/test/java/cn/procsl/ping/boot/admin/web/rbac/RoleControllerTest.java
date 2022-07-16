package cn.procsl.ping.boot.admin.web.rbac;

import cn.procsl.ping.boot.admin.TestAdminApplication;
import cn.procsl.ping.boot.admin.web.LoginUtils;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.javafaker.Faker;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = TestAdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class RoleControllerTest {

    @Autowired
    MockMvc mockMvc;

    JsonMapper jsonMapper = new JsonMapper();
    AtomicLong gid = new AtomicLong();

    AtomicLong pid = new AtomicLong();

    MockHttpSession session;

    @BeforeEach
    public void beforeEach() throws Exception {
        this.session = LoginUtils.toLogin(mockMvc);

        val permission = JMockData.mock(PermissionCreateDTO.class);
        permission.setType(PermissionType.page);

        mockMvc.perform(
                       post("/v1/permissions")
                               .contentType(APPLICATION_JSON)
                               .content(jsonMapper.writeValueAsString(permission))
                               .session(session)
               )
               .andExpect(status().is2xxSuccessful())
               .andDo(result -> {
                   String str = result.getResponse().getContentAsString();
                   PermissionVO permissionVO = this.jsonMapper.readValue(str, PermissionVO.class);
                   log.info("PermissionVO:{}", permissionVO);
                   pid.set(permissionVO.getId());
               });


        RoleDetailsDTO role = new RoleDetailsDTO(Faker.instance().name().fullName(), List.of(pid.get()));
        mockMvc.perform(post("/v1/roles")
                       .contentType(APPLICATION_JSON)
                       .content(jsonMapper.writeValueAsString(role))
                       .session(session)
               )
               .andExpect(status().is2xxSuccessful())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andDo(result -> {
                   String str = result.getResponse().getContentAsString();
                   RoleVO roleVo = this.jsonMapper.readValue(str, RoleVO.class);
                   gid.set(roleVo.getId());
               });
        log.info("BeforeEach is end!");
    }


    @Test
    public void create() throws Exception {
        Collection<Long> permission = List.of(pid.get());
        RoleDetailsDTO role = new RoleDetailsDTO(Faker.instance().name().fullName(), permission);
        mockMvc.perform(post("/v1/roles")
                       .contentType(APPLICATION_JSON)
                       .content(jsonMapper.writeValueAsString(role))
                       .session(session)
               )
               .andExpect(status().is2xxSuccessful())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andDo(result -> {
                   String str = result.getResponse().getContentAsString();
                   log.info("roleVo:{}", str);
               });
    }

    @Test
    public void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/roles/{id}", gid.get())
                                              .session(session)
               )
               .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void change() throws Exception {
        RoleDetailsDTO role = new RoleDetailsDTO(Faker.instance().name().fullName(), List.of(pid.get()));
        mockMvc.perform(patch("/v1/roles/{id}", gid.get())
                       .contentType(APPLICATION_JSON)
                       .content(jsonMapper.writeValueAsString(role))
                       .session(session)
               )
               .andExpect(status().is2xxSuccessful());


        role.setPermissions(List.of(pid.get()));
        role.setName("test");
        mockMvc.perform(patch("/v1/roles/{id}",
                       gid.get())
                       .contentType(APPLICATION_JSON)
                       .content(jsonMapper.writeValueAsString(role))
                       .session(session)
               )
               .andExpect(status().is2xxSuccessful());

    }

    @Test
    public void getById() throws Exception {
        mockMvc.perform(get("/v1/roles/{id}", gid.get())
                       .accept(APPLICATION_JSON)
                       .session(session)
               )
               .andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
               .andExpect(jsonPath("$.name").isNotEmpty())
               .andExpect(jsonPath("$.permissions").isArray());

    }


    @Test
    public void findRoles() throws Exception {
        val builder = get("/v1/roles")
                .param("limit", "1")
                .param("sort", "id", "desc")
                .session(session)
                .accept(APPLICATION_JSON);

        mockMvc.perform(builder)
               .andExpect(status().isOk())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andExpect(jsonPath("$.content").isNotEmpty())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.limit").value("1"))
               .andExpect(jsonPath("$.empty").value("false"))
               .andDo(print());
    }
}