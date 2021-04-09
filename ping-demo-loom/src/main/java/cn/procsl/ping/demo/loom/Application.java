package cn.procsl.ping.demo.loom;

import java.util.concurrent.CountDownLatch;

public class Application {


    public static void main(String[] args) {
        var latch = new CountDownLatch(10);
        for (int i = 0; i < 10000; i++) {
            Thread.startVirtualThread(() -> {
                System.out.println("Hello from Thread " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                latch.countDown();
            });
        }
        try {
            latch.await();
            System.out.println("All Threads done");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
