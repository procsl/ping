package cn.procsl.ping.boot.system.auth.login;

import cn.procsl.ping.boot.system.TestSystemApplication;
import cn.procsl.ping.boot.system.domain.auth.Authentication;
import cn.procsl.ping.boot.system.domain.auth.AuthenticationSpecification;
import cn.procsl.ping.boot.system.domain.auth.AuthenticationState;
import cn.procsl.ping.boot.system.web.LoginUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.inject.Inject;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = TestSystemApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AuthenticationControllerTest {

    @Inject
    MockMvc mockMvc;

    MockHttpSession session;

    @Test
    @Order(1)
    public void createSession() throws Exception {
        session = LoginUtils.toLogin(mockMvc);
    }

    @Test
    @Order(2)
    public void deleteSession() throws Exception {
        MockHttpServletRequestBuilder processor = delete("/v1/session").session(this.session);
        mockMvc.perform(processor).andExpect(status().is2xxSuccessful());
        log.info("注销成功");
    }

    @Inject
    JpaSpecificationExecutor<Authentication> jpaSpecificationExecutor;

    @Test
    public void currentSession() {
        Optional optional = this.jpaSpecificationExecutor.findOne(
                new AuthenticationSpecification("1234", AuthenticationState.online));
    }
}