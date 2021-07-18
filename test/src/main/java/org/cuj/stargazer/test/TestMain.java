package org.cuj.stargazer.test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMain {
    public static void main(String[] args) {
        log.info("test main");
        sayHello();
        sayHello2("hello world222222222");
        new TestOtherMethod().print(" \" this is TestOtherMethod \"");
        runTread();
    }

    private static void runTread() {
        for(int i=0;i<10;++i){
            new TestMethodThread(Integer.toString(i)).start();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sayHello() {
        try {
            Thread.sleep(2000);
            log.info("hello world!!");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sayHello2(String hello) {
        try {
            Thread.sleep(1000);
            log.info(hello);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
