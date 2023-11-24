package cn.procsl.ping.boot.ui.domain;

import com.google.common.base.Stopwatch;
import lombok.Getter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class ComponentTest {

    @Test
    public void main() {
        Stopwatch started = Stopwatch.createUnstarted();
        started.start();
        User user = null;
        for (long i = 0; i < 1000_000_000; i++) {
            user = new User(1, "bb");
        }
        started.stop();
        System.out.println(started.elapsed(TimeUnit.MILLISECONDS) + "ms");
        //不加打印 300ms
        //加了打印 3000ms
//        System.out.println(user);
//        user.toString();
        Runnable aa = user::getUserName;
    }

    @Test
    public void main2() {
        long start = System.currentTimeMillis();
        User user = null;
        for (long i = 0; i < 1000_000_000; i++) {
            user = new User(1, "bb");
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
//        user.toString();
    }

    @Getter
    static class User {

        @Override
        public String toString() {
            return "";
        }

        private int age;
        private String userName;

        public User(int age, String userName) {
            this.age = age;
            this.userName = userName;
            userName.toString();
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

}