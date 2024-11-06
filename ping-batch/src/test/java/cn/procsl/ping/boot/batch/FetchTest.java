package cn.procsl.ping.boot.batch;

import cn.procsl.ping.boot.batch.item.Fetch;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FetchTest {

    @Test
    public void run() throws IOException, InterruptedException {
        Fetch fetch = new Fetch();
        fetch.run();
    }

}
