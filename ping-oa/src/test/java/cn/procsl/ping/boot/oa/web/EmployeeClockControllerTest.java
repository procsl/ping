package cn.procsl.ping.boot.oa.web;

import cn.procsl.ping.boot.oa.TestOaApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = TestOaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class EmployeeClockControllerTest {

    @Inject
    MockMvc mockMvc;

    @Test
    public void addClock() throws Exception {
        mockMvc.perform(
                       post("/v1/oa/clock")
                               .contentType(APPLICATION_JSON)
               )
               .andExpect(status().is2xxSuccessful())
               .andDo(result -> {
                   String str = result.getResponse().getContentAsString();
                   log.debug("响应体为:{}", str);
               })
               .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getClock() {
    }
}