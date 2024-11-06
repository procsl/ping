package cn.procsl.ping.batch;

import cn.procsl.ping.batch.item.Fetch;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FetchTest {

    @Test
    public void run() throws IOException, InterruptedException {
        Fetch fetch = new Fetch();
        fetch.run();
    }

}
