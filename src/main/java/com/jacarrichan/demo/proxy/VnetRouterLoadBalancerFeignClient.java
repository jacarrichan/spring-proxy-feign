package com.jacarrichan.demo.proxy;

import feign.Client;
import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.feign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;

import java.io.IOException;

import static com.jacarrichan.demo.proxy.DemoConstants.PROXY_SERVICE_ID;

@Slf4j
public class VnetRouterLoadBalancerFeignClient extends LoadBalancerFeignClient {
    private final static String SERVICE_ID_START = "://";

    public VnetRouterLoadBalancerFeignClient(Client delegate,
                                             CachingSpringLoadBalancerFactory lbClientFactory,
                                             SpringClientFactory clientFactory) {
        super(delegate, lbClientFactory, clientFactory);
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        StringBuilder url = new StringBuilder(request.url());
        //插入 proxy name
        url = url.insert(url.indexOf(SERVICE_ID_START) + SERVICE_ID_START.length(), PROXY_SERVICE_ID + "/");
        request = Request.create(request.method(), url.toString(), request.headers(), request.body(), request.charset());
        log.info("{}", request.toString());
        return super.execute(request, options);
    }
}
