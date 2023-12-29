package cn.procsl.ping.boot.web.cipher;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.MimeType;

import javax.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CipherFilterTest {

    @Inject
    MockMvc mockMvc;

    @Test
    public void mimeTypeTest() {
        MimeType mime = MimeType.valueOf("application/vnd.enc;encode=base64;origin=encode-type");
        log.info("type: {}, sub: {}, params={}", mime.getType(), mime.getSubtype(), mime.getParameters());

        MimeType mimeType = MimeType.valueOf("application/vnd.enc");

        Assertions.assertTrue(mimeType.equalsTypeAndSubtype(mime));

    }


    @Test
    @DisplayName("非加密 APPLICATION_FORM_URLENCODED_VALUE 测试")
    public void testCipherParameterPostRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/test/cipher/parameter")
                        .param("a", "1")
                        .param("b", "2")
                        .param("c", "3")
                        .header("Content-Type", APPLICATION_FORM_URLENCODED_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.a").value("1"))
                .andExpect(jsonPath("$.b").value("2"))
                .andExpect(jsonPath("$.c").value("3"));

        mockMvc.perform(MockMvcRequestBuilders.post("/test/cipher/parameter")
                        .header("Content-Type", APPLICATION_FORM_URLENCODED_VALUE)
                        .content("a=1&b=2&c=3")
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.a").value("1"))
                .andExpect(jsonPath("$.b").value("2"))
                .andExpect(jsonPath("$.c").value("3"));

    }


    @DisplayName("非加密 json 测试")
    @Test
    public void testPostRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/test/cipher/body")
                        .param("a", "1")
                        .param("b", "2")
                        .param("c", "3")
                        .header("Content-Type", APPLICATION_JSON)
                        .content("{\"a\":\"4\",\"b\":\"5\",\"c\":\"6\"}")
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.a").value("4"))
                .andExpect(jsonPath("$.b").value("5"))
                .andExpect(jsonPath("$.c").value("6"));

        mockMvc.perform(MockMvcRequestBuilders.post("/test/cipher/body")
                        .header("Content-Type", APPLICATION_JSON)
                        .content("{\"a\":\"4\",\"b\":\"5\",\"c\":\"6\"}")
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.a").value("4"))
                .andExpect(jsonPath("$.b").value("5"))
                .andExpect(jsonPath("$.c").value("6"));

    }

    @Test
    @DisplayName("加密 json 测试")
    public void testPostRequest2() throws Exception {

        MimeType mimeType = MimeType.valueOf("application/vnd.enc;encode=origin;origin=%s".formatted(URLEncoder.encode(APPLICATION_JSON.toString(), StandardCharsets.UTF_8)));

        mockMvc.perform(MockMvcRequestBuilders.post("/test/cipher/body")
                        .header("Content-Type", mimeType.toString())
                        .content("{\"a\":\"4\",\"b\":\"5\",\"c\":\"6\"}")
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.a").value("4"))
                .andExpect(jsonPath("$.b").value("5"))
                .andExpect(jsonPath("$.c").value("6"));
    }

    @Test
    @DisplayName("加密 www 测试")
    public void testContentType() throws Exception {
        MimeType mimeType = MimeType.valueOf("application/vnd.enc;encode=origin;origin=%s"
                .formatted(URLEncoder.encode(APPLICATION_FORM_URLENCODED_VALUE, StandardCharsets.UTF_8)));

        mockMvc.perform(MockMvcRequestBuilders.post("/test/cipher/parameter")
                        .header("Content-Type", mimeType.toString())
                        .content("a=1&b=2&c=3")
                )
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.a").value("4"))
                .andExpect(jsonPath("$.b").value("5"))
                .andExpect(jsonPath("$.c").value("6"));
    }

}