package cn.procsl.ping.app.query;

import cn.procsl.ping.app.DistributeApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Slf4j
@Rollback
@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = DistributeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class TestControllerTest {


    @Inject
    MockMvc mockMvc;

    @org.junit.jupiter.api.Test
    public void echo() throws Exception {

        mockMvc.perform(
                get("/v1/echo?input=Hello World")
            )
            .andDo(result -> {
                String str = result.getResponse().getContentAsString();
                log.debug("响应体为:{}", str);
            });
        log.info("BeforeEach is end!");

    }
}
