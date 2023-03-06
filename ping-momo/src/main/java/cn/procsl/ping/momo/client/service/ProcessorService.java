package cn.procsl.ping.momo.client.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class ProcessorService {

    final String index_url = "https://panduoduo.yangkeduo.com/";

    public ActionType processor(Map<String, Object> data) {
        String type = (String) data.get("type");
        if (type.equals("init")) {
            return onStart(data);
        }
        return ActionType.start;
    }

    public ActionType onStart(Map<String, Object> data) {
        Object tmp = data.get("url");
        if (tmp == null) {
            log.warn("数据错误, 停止抓取");
            return ActionType.stop;
        }
        String url = tmp.toString();
        if (url.startsWith(index_url)) {
            return ActionType.start;
        }
        log.info("非主页不抓取:{}", url);
        return ActionType.stop;
    }

}
