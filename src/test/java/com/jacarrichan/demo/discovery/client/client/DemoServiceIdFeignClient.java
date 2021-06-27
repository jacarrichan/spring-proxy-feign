package com.jacarrichan.demo.discovery.client.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import static com.jacarrichan.demo.proxy.DemoConstants.SERVICE_ID;

/**
 * @Author by AA2668 on 2021/6/24.
 */
@Service
@FeignClient(name = "DemoServiceIdFeignClient", serviceId = SERVICE_ID)
public interface DemoServiceIdFeignClient {
    @PostMapping(value = "/hello", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String hello(String name);

    @PostMapping(value = "/hello", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String sayhello(String name);
}
