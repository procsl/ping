package cn.procsl.ping.boot.batch.job;


import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FetchJobTest {

    @Test
    public void run() throws IOException, InterruptedException {
        FetchJob fetchJob = new FetchJob();
        fetchJob.run();
    }

}
