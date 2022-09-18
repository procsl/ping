package cn.procsl.ping.boot.admin.web.rbac;

import cn.procsl.ping.boot.admin.TestAdminApplication;
import cn.procsl.ping.boot.admin.web.LoginUtils;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.jsonzou.jmockdata.JMockData;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@Rollback
@AutoConfigureMockMvc
@SpringBootTest(classes = TestAdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PermissionControllerTest {

    @Inject
    MockMvc mockMvc;
    JsonMapper jsonMapper = new JsonMapper();
    AtomicLong gid = new AtomicLong();

    MockHttpSession session;

    @BeforeEach
    public void setUp() throws Exception {
        session = LoginUtils.toLogin(mockMvc);

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
                   gid.set(permissionVO.getId());
               });

    }

    @Test
    public void create() throws Exception {
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
               });
    }

    @Test
    public void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                       .delete("/v1/permissions/{id}", gid.get())
                       .session(session)
               )
               .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void update() throws Exception {
        val permission = JMockData.mock(PermissionUpdateDTO.class);
        mockMvc.perform(MockMvcRequestBuilders
                       .patch("/v1/permissions/{id}", gid.get())
                       .contentType(APPLICATION_JSON)
                       .content(jsonMapper.writeValueAsString(permission))
                       .session(session)
               )
               .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void findPermissions() throws Exception {
        log.info("测试所有类型");
        mockMvc.perform(
                       get("/v1/permissions")
                               .session(session)
               )
               .andExpect(status().isOk())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andExpect(jsonPath("$.content").isNotEmpty())
               .andExpect(jsonPath("$.content").isArray())
               .andExpect(jsonPath("$.content[*].id").isNotEmpty())
               .andExpect(jsonPath("$.content[*].operate").isNotEmpty())
               .andExpect(jsonPath("$.content[*].resource").isNotEmpty())
               .andExpect(jsonPath("$.empty").value("false"));
        log.info("测试HTTP类型");
        mockMvc.perform(
                       get("/v1/permissions")
                               .session(session)
                               .param("limit", "1")
                               .param("type", "http")
               )
               .andExpect(status().isOk());

        log.info("测试Page类型");
        mockMvc.perform(
                       get("/v1/permissions")
                               .session(session)
                               .param("limit", "1")
                               .param("type", "page")
               )
               .andExpect(status().isOk());

    }
}