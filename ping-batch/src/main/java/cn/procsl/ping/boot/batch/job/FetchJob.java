package cn.procsl.ping.boot.batch.job;

import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class FetchJob {


    @Scheduled(fixedDelay = 60 * 60 * 1000, initialDelay = 0)
    public void run() throws IOException, InterruptedException {


        try (
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
            InputStream is = new FileInputStream("C:\\Users\\procsl\\AppData\\Roaming\\JetBrains\\IntelliJIdea2023.3\\scratches\\查询.json");
            FileOutputStream file = new FileOutputStream("C:\\Users\\procsl\\Desktop\\output\\file.json");
        ) {

            final JsonProvider jsonProvider = new JacksonJsonProvider();
            final MappingProvider mappingProvider = new JacksonMappingProvider();
            Configuration conf = Configuration.builder().jsonProvider(jsonProvider)
                .mappingProvider(mappingProvider)
                .options(Option.ALWAYS_RETURN_LIST).build();

            String json = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
            DocumentContext context = JsonPath.using(conf).parse(json);

            ZonedDateTime now = ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            String iso8601 = now.format(DateTimeFormatter.ISO_INSTANT);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDate = now.format(formatter);

            log.info("当前时间: {}, {}", iso8601, formattedDate);
            ZonedDateTime now2 = ZonedDateTime.now().withHour(0).withMinute(30).withSecond(0).withNano(0);

            context.set("$.query.bool.filter[2].range['@timestamp'].gte", iso8601);
            context.set("$.query.bool.filter[2].range['@timestamp'].lte", now2.format(DateTimeFormatter.ISO_INSTANT));
            context.set("$.size", 10000);

            String newJson = context.jsonString();
            log.info("json string: {}", newJson);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://10.68.138.58/api/console/proxy?path=mcs_customer_web-2024.11*%2F_search&method=POST"))
                .POST(BodyPublishers.ofString(newJson))
                .setHeader("Accept", "text/plain, */*; q=0.01")
                .setHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6")
                .setHeader("Content-Type", "application/json")
                .setHeader("kbn-version", "7.4.2")
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            if (!success) {
                log.warn("请求失败: {}", response.body());
                return;
            }

            Optional<String> type = response.headers().firstValue("Content-Type");
            if (type.isEmpty() || !type.get().contains("application/json")) {
                log.warn("数据类型错误: {}", response.body());
                return;
            }
            String body = response.body();
            log.info("响应结果: {}", body);
            file.write(response.body().getBytes(StandardCharsets.UTF_8));

            DocumentContext message = JsonPath.using(conf).parse(body);

            TypeRef<List<String>> typeRef = new TypeRef<>() {
            };

            List<String> messages = message.read("$.hits.hits[*]._source.message", typeRef);
            log.info("jsonString: {}", messages);

            // 创建Pattern对象
            Pattern pattern = Pattern.compile("\\{([^}]*)}");
            HashSet<String> results = new HashSet<>();
            for (String text : messages) {
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    results.add(matcher.group(1));
                }
            }
            log.info("results: {}", results);

        } finally {
            log.info("请求完成");
        }
    }

}
