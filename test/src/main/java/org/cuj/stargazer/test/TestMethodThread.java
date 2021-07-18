package org.cuj.stargazer.test;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestMethodThread extends Thread{
    private String name;
    public TestMethodThread(String name){
        this.name = name;
    }
    public void run(){
        print();
    }

    private void print(){
        log.info("the info is thread "+name);
    }
}
