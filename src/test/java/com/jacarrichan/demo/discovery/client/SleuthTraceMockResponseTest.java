package com.jacarrichan.demo.discovery.client;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DemoServiceMockResponseTest.TestConfig.class, webEnvironment = DEFINED_PORT, value = {
        "spring.application.name=feignclienturltest", "feign.hystrix.enabled=false", "feign.okhttp.enabled=false",
        "spring.mvc.servlet.load-on-startup=1", "liquibase.enabled=false",
        "eureka.client.enabled=true"
})
@DirtiesContext
public class SleuthTraceMockResponseTest extends DemoServiceMockResponseTest {
}
