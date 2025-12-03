package cn.procsl.ping.boot.mhpp;

import cn.procsl.ping.boot.mhpp.protocol.TunnelClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TunnelClientBootstrap {

    public static void main(String[] args) throws InterruptedException {

        TunnelClient service = new TunnelClient();
        service.start();

    }


}
