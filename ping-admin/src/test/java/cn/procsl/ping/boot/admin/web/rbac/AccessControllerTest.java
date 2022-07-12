package cn.procsl.ping.boot.admin.web.rbac;

import cn.procsl.ping.boot.admin.TestAdminApplication;
import cn.procsl.ping.boot.admin.web.LoginUtils;
import cn.procsl.ping.boot.admin.web.user.RegisterDTO;
import cn.procsl.ping.boot.admin.web.user.UserController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.jsonzou.jmockdata.JMockData;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.jsonzou.jmockdata.JMockData.mock;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = TestAdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AccessControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserController userController;

    JsonMapper jsonMapper = new JsonMapper();
    List<String> uid = new ArrayList<>();

    List<String> rid = new ArrayList<>();

    AtomicLong gid = new AtomicLong();

    @BeforeEach
    void setUp() throws Exception {
        MockConfig config = new MockConfig().stringRegex("[a-zA-Z0-9_]{5,20}");
        for (int i = 0; i < 10; i++) {
            String account = mock(String.class, config);
            RegisterDTO user = new RegisterDTO(account, "password");
            mockMvc.perform(
                            post("/v1/users")
                                    .contentType(APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(user))
                                    .session(LoginUtils.toLogin(mockMvc))
                    )
                    .andDo(result -> {
                        String str = result.getResponse().getContentAsString();
                        uid.add(str);
                    });

            List<Long> pid = new ArrayList<>();
            {
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
                            PermissionVO permissionVO = this.jsonMapper.readValue(str, PermissionVO.class);
                            log.info("PermissionVO:{}", permissionVO);
                            pid.add(permissionVO.getId());
                        });
            }

            RoleDetailsDTO role = new RoleDetailsDTO(mock(String.class, config), pid);
            mockMvc.perform(
                            post("/v1/roles")
                                    .contentType(APPLICATION_JSON)
                                    .content(jsonMapper.writeValueAsString(role))
                                    .session(LoginUtils.toLogin(mockMvc))
                    )
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andDo(result -> rid.add(role.getName()));
        }
        log.info("BeforeEach is end! uid:{}, rid:{}", uid, rid);
    }

    @Test
    void grant() throws Exception {
        MockConfig intRange = new MockConfig().intRange(0, uid.size() - 1);
        MockConfig nameRange = new MockConfig().intRange(1, uid.size() - 1);
        Integer index1 = mock(Integer.class, nameRange);
        Integer index2 = mock(Integer.class, nameRange);
        List<String> body;
        if (index1 < index2) {
            body = rid.subList(index1, index2);
        } else if (index1 > index2) {
            body = rid.subList(index2, index1);
        } else {
            body = rid.subList(0, 1);
        }
        gid.set(Long.parseLong(uid.get(JMockData.mock(Integer.class, intRange))));
        String json = jsonMapper.writeValueAsString(body);
        mockMvc.perform(
                        post("/v1/users/{id}/roles", gid.get())
                                .contentType(APPLICATION_JSON)
                                .content(json)
                                .session(LoginUtils.toLogin(mockMvc))
                )
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void findSubjects() throws Exception {
        this.grant();
        mockMvc.perform(get("/v1/users/{id}/roles", gid.get())
                        .session(LoginUtils.toLogin(mockMvc))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty())
                .andDo(print())
                .andExpect(result -> {
                    String json = result.getResponse().getContentAsString();
                    JsonNode tree = this.jsonMapper.readTree(json);
                    for (JsonNode node : tree) {
                        String name = node.get("name").asText();
                        Assertions.assertTrue(rid.contains(name));
                    }
                });
    }


}