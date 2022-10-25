package cn.procsl.ping.boot.captcha.web;

import cn.procsl.ping.boot.captcha.TestCaptchaApplication;
import cn.procsl.ping.boot.captcha.domain.*;
import cn.procsl.ping.boot.captcha.domain.email.EmailCaptcha;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptchaBuilderService;
import cn.procsl.ping.boot.captcha.domain.image.ImageCaptchaRepository;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.procsl.ping.boot.captcha.domain.image.ImageCaptcha.token_key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(classes = TestCaptchaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CaptchaControllerTest {


    @Inject
    MockMvc mockMvc;

    @Inject
    ImageCaptchaRepository imageCaptchaRepository;

    @Inject
    JpaSpecificationExecutor<ImageCaptcha> specificationExecutor;

    @Inject
    JpaSpecificationExecutor<EmailCaptcha> emailCaptchaJpaSpecificationExecutor;

    JsonMapper jsonMapper = new JsonMapper();

    ImageCaptchaBuilderService imageCaptchaBuilderService = new ImageCaptchaBuilderService();

    @Test
    public void createImageCaptcha() throws Exception {

        MockHttpSession session = new MockHttpSession();
        AtomicReference<Cookie[]> cookiesReference = new AtomicReference<>();
        AtomicReference<ImageCaptcha> imageReference = new AtomicReference<>();

        mockMvc.perform(post("/v1/captcha/image").session(session))
               .andExpect(status().is2xxSuccessful())
               .andDo(result -> {
                   byte[] str = result.getResponse().getContentAsByteArray();
                   String res = Base64.getEncoder().encodeToString(str);
                   log.info("响应体为:{}", res);
                   Assertions.assertNotNull(res);
                   Cookie cookie = result.getResponse().getCookie(token_key);
                   Assertions.assertNotNull(cookie);
                   String value = cookie.getValue();
                   imageReference.set(imageCaptchaBuilderService.buildForToken("123456", value));
                   cookiesReference.set(result.getResponse().getCookies());
               });

        byte[] ticket = imageReference.get().getTicket().getBytes(StandardCharsets.UTF_8);
        String code = Base64.getEncoder().encodeToString(ticket);
        MockHttpServletRequestBuilder getMap = get("/v1/test")
                .header(VerifyCaptcha.header, code)
                .session(session).cookie(cookiesReference.get());

        mockMvc.perform(getMap).andExpect(status().is2xxSuccessful());
        mockMvc.perform(getMap).andExpect(status().is2xxSuccessful());
        mockMvc.perform(getMap).andExpect(status().is2xxSuccessful());
        mockMvc.perform(getMap).andExpect(status().is2xxSuccessful());
        mockMvc.perform(getMap).andExpect(status().is2xxSuccessful());

        List<ImageCaptcha> res = this.specificationExecutor.findAll(
                (root, query, cb) -> cb.equal(root.get("target"), session.getId()));

        Assertions.assertNotEquals(res.size(), 0);
    }

    @Test
    public void sendEmailCaptcha() throws Exception {
        mockMvc.perform(post("/v1/captcha/email"))
               .andExpect(status().is4xxClientError());

        MockHttpSession session = new MockHttpSession();
        AtomicReference<Cookie[]> cookiesReference = new AtomicReference<>();
        AtomicReference<ImageCaptcha> imageReference = new AtomicReference<>();
        mockMvc.perform(post("/v1/captcha/image").session(session))
               .andDo(result -> {
                   Cookie cookie = result.getResponse().getCookie(token_key);
                   Assertions.assertNotNull(cookie);
                   String value = cookie.getValue();
                   imageReference.set(imageCaptchaBuilderService.buildForToken("123456", value));
                   cookiesReference.set(result.getResponse().getCookies());
               })
               .andExpect(status().is2xxSuccessful());


        String json = jsonMapper.writeValueAsString(new EmailSenderDTO("test@email.com"));
        MockHttpServletRequestBuilder toPost = post("/v1/captcha/email")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(cookiesReference.get())
                .header(VerifyCaptcha.header, Base64.getEncoder().encodeToString(
                        imageReference.get().getTicket().getBytes(StandardCharsets.UTF_8)))
                .session(session);

        mockMvc.perform(toPost)
               .andExpect(status().is2xxSuccessful());
    }


}