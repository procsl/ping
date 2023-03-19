package cn.procsl.ping.momo.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PageTransformService {

    Map<String, String> pages = new HashMap<>();

    public void savePage(String url, String page) {
        pages.put(url, page);
    }

}
