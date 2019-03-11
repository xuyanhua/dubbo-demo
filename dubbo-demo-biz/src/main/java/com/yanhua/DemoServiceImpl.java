package com.yanhua;


public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        System.out.println("hello:" + name);
        return "hello:" + name;
    }
}
