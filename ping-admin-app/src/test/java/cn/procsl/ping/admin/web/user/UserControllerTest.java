package cn.procsl.ping.admin.web.user;


import cn.procsl.ping.admin.AdminApplication;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
@SpringBootTest(classes = AdminApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Rollback
public class UserControllerTest {


    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserController userController;

    JsonMapper jsonMapper = new JsonMapper();

    @Test
    void test() {
        Assertions.assertNotNull(userController);
    }

    @Test
    public void register() throws Exception {
        UserDTO user = new UserDTO("program_chen@foxmail.com", "password");
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonMapper.writeValueAsBytes(user))
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> {
                });
    }
}