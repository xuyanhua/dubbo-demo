package com.yanhua;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.EchoService;
import com.alibaba.dubbo.rpc.service.GenericService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author xuyanhua
 * @description:
 * @date 2019/3/11 下午3:35
 */
public class CallTest {

    private DemoService demoService;
    private ClassPathXmlApplicationContext context;
    private final static Logger logger = LoggerFactory.getLogger(CallTest.class);

    @Before
    public void init() {
        context = new ClassPathXmlApplicationContext(new String[]{"consumer.xml"});
        context.start();
        demoService = (DemoService) context.getBean("demoService");

    }

    /**
     * 普通调用
     */
    @Test
    public void testCall() {
        String hello = demoService.sayHello("xuyh");
        logger.info("返回信息-->" + hello);
    }

    /**
     * api调用
     */
    @Test
    public void testCallApi() {
        // 当前应用配置
        ApplicationConfig application = new ApplicationConfig();
        application.setName("demo-consumer2");

        // 连接注册中心配置
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("127.0.0.1:2181");

        // 注意：ReferenceConfig为重对象，内部封装了与注册中心的连接，以及与服务提供方的连接

        // 引用远程服务
        ReferenceConfig<DemoService> reference = new ReferenceConfig<>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
        reference.setApplication(application);
        reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
        reference.setInterface(DemoService.class);
        reference.setVersion("1.0.0");

        // 和本地bean一样使用xxxService
        DemoService demoService = reference.get(); // 注意：此代理对象内部封装了所有通讯细节，对象较重，请缓存复用
        String hello = demoService.sayHello("xuyh2");
        logger.info("返回信息-->" + hello);
    }


    /**
     * 声明式泛化引用
     */
    @Test
    public void testGeneric() {
        GenericService barService = (GenericService) context.getBean("demoServiceGeneric");
        Object result = barService.$invoke("sayHello", new String[]{"java.lang.String"}, new Object[]{"World"});
        logger.info("返回值：" + result);
    }


    /**
     * Api式泛化调用
     */
    @Test
    public void testGenericApi() {
        // 当前应用配置
        ApplicationConfig application = new ApplicationConfig();
        application.setName("demo-consumer2");

        // 连接注册中心配置
        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("zookeeper://127.0.0.1:2181");

        // 注意：ReferenceConfig为重对象，内部封装了与注册中心的连接，以及与服务提供方的连接

        // 引用远程服务
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
        reference.setApplication(application);
        reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
        reference.setInterface("com.yanhua.DemoService");
        reference.setGeneric(true);

        // 用org.apache.dubbo.rpc.service.GenericService可以替代所有接口引用
        GenericService genericService = reference.get();

        // 基本类型以及Date,List,Map等不需要转换，直接调用
        Object result = genericService.$invoke("sayHello", new String[]{"java.lang.String"}, new Object[]{"world"});

        System.out.println("api调用返回：" + result);
        // 用Map表示POJO参数，如果返回值为POJO也将自动转成Map
//        Map<String, Object> person = new HashMap<String, Object>();
//        person.put("name", "xxx");
//        person.put("password", "yyy");
//// 如果返回POJO将自动转成Map
//        Object result = genericService.$invoke("findPerson", new String[]{"com.xxx.Person"}, new Object[]{person});
    }


    /**
     * 回声测试
     */
    @Test
    public void testEcho() {
        EchoService echoService = (EchoService) context.getBean("demoService");
        Object status = echoService.$echo("OK");//输入什么就返回什么
        logger.info("status---->" + status);
        assert (status.equals("OK"));

    }


}
