package cn.procsl.ping.boot.common.utils;

import com.google.common.hash.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;

@Slf4j
public class BloomFilterTest {


    @RepeatedTest(10000)
    public void test() {

        ArrayList<String> list = new ArrayList<>();

        for (int i = 0; i < 100000; i++) {
            String str = Hashing.murmur3_128(1).hashLong(i).toString();
            list.add(str);
        }

        HashSet<String> res = new HashSet<>(list);
        Assertions.assertEquals(res.size(), list.size(), "无冲突");
    }

    @RepeatedTest(1)
    @SuppressWarnings("all")
    public void bloomFilterTest() throws IOException {

        int start = 1000000;
        HashSet<Long> cont = new HashSet<>();
        BloomFilter<Long> filter = BloomFilter.create(Funnels.longFunnel(), 1000000, 0.0001);

        for (int i = start; i > 0; i--) {
            boolean bool = filter.mightContain((long) i);
            if (bool) {
                cont.add((long) i);
            }
            filter.put((long) i);
        }
        log.info("冲突元素: {}", cont);
    }

}
