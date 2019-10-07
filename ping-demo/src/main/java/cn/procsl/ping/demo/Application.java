package cn.procsl.ping.demo;

import java.util.ServiceLoader;

/**
 * @author procsl
 * @date 2019/10/07
 */
public class Application {

    public static void main(String[] args) {
        ServiceLoader<ClassLoadDemo> generators = ServiceLoader.load(ClassLoadDemo.class);
    }
}
