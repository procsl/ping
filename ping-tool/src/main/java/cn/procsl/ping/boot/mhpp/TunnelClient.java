package cn.procsl.ping.boot.mhpp;

import cn.procsl.ping.boot.mhpp.protocol.TunnelManagerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TunnelClient {

    public static void main(String[] args) throws InterruptedException {

        TunnelManagerService service = new TunnelManagerService();
        service.start();

    }


}
