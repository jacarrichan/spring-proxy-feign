package com.jacarrichan.demo.discovery.client;

import com.jacarrichan.demo.discovery.client.client.DemoServiceIdFeignClient;
import com.jacarrichan.demo.discovery.client.client.DemoUrlFeignClient;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.LoadBalancerStats;
import com.netflix.loadbalancer.NoOpLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.SocketUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.jacarrichan.demo.proxy.DemoConstants.SERVICE_ID;

/**
 * @Author by AA2668 on 2020/9/20.
 */
@Slf4j
public abstract class DemoServiceMockResponseTest {
    public static int port;
    @Resource
    private DemoUrlFeignClient demoUrlFeignClient;
    @Resource
    private DemoServiceIdFeignClient demoServiceIdFeignClient;


    @BeforeClass
    public static void beforeClass() {
        port = SocketUtils.findAvailableTcpPort();
        System.setProperty("server.port", String.valueOf(port));
        System.setProperty("demo.hostAddr", "localhost:" + port);
    }

    @Test
    public void testDirectRequestHello() {
        //断言没有被proxy代理
        Assert.assertFalse(demoUrlFeignClient.hello("test").contains("demo-zuul"));
        Assert.assertFalse(demoUrlFeignClient.sayhello("test").contains("demo-zuul"));
    }

    @Test
    public void testProxyRequestHello() {
        //断言被proxy代理
        Assert.assertTrue(demoServiceIdFeignClient.hello("test").contains("demo-zuul"));
        Assert.assertTrue(demoServiceIdFeignClient.sayhello("test").contains("demo-zuul"));
    }


    @Configuration()
    @EnableAutoConfiguration()
    @RestController
    @EnableFeignClients(clients = {DemoUrlFeignClient.class, DemoServiceIdFeignClient.class})
//    @Import(value = {})
    @ComponentScan(value = {"com.jacarrichan.demo"})
    protected static class TestConfig {
        @Autowired
        private HttpServletRequest servletRequest;

        @PostMapping(value = "hello", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
        String hello(String name) {
            log.warn("收到直接的请求，返回mock 响应报文");
            return "hello" + name;
        }

        @PostMapping(value = SERVICE_ID + "/hello", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
        String proxyHello(String name) {
            log.warn("收到来自demo-zuul转发的请求，返回mock 响应报文");
            return "hello" + name + ",proxy by demo-zuul";
        }
    }

    @Bean
    ILoadBalancer ribbonLoadBalancer() {
        return new MockLoadBalancer();
    }


    /**
     * 路由回mock服务
     */
    static class MockLoadBalancer extends NoOpLoadBalancer {
        @Override
        public Server chooseServer(Object key) {
            log.info("key:{}", key);
            return new Server("http", "localhost", port);
        }

        @Override
        public LoadBalancerStats getLoadBalancerStats() {
            return new LoadBalancerStats("local");
        }
    }
}