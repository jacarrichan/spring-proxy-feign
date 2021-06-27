package com.jacarrichan.demo.proxy;

import feign.Client;
import feign.InvocationHandlerFactory;
import feign.Target;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.netflix.feign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;


@Slf4j
@Configuration
@ConditionalOnClass(LoadBalancerFeignClient.class)
public class VnetRouterFeignAutoConfiguration implements BeanPostProcessor {
    @Resource
    CachingSpringLoadBalancerFactory lbClientFactory;
    @Resource
    SpringClientFactory clientFactory;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {

        tryReplaceFeignClientLoadBalance(bean);
        //说明没有被sleuth增强过
        if (bean instanceof LoadBalancerFeignClient) {
            log.info("当前实例是 {},开始增强LoadBalance功能", LoadBalancerFeignClient.class);
            return new VnetRouterLoadBalancerFeignClient(((LoadBalancerFeignClient) bean).getDelegate(), lbClientFactory, clientFactory);
        }
        return bean;
    }

    /**
     * 如果是feign client的实例 ,则找出里面的FeignInvocationHandler属性，并将其属性dispatch的SynchronousMethodHandler给增强
     * <p>
     * <p>
     * SynchronousMethodHandler
     *
     * @param bean
     */
    private void tryReplaceFeignClientLoadBalance(Object bean) {
        if (!Proxy.isProxyClass(bean.getClass())) {
            return;
        }
        Object invocationHandler = Proxy.getInvocationHandler(bean);
        Field targetField = ReflectionUtils.findField(invocationHandler.getClass(), "target");
        if (null == targetField) {
            return;
        }
        targetField.setAccessible(true);
        Object val = ReflectionUtils.getField(targetField, invocationHandler);
        if (null == val) {
            return;
        }
        if (!(Target.class.isAssignableFrom(val.getClass()))) {
            return;
        }
        replaceFeignClientLoadBalance(invocationHandler);
    }

    private void replaceFeignClientLoadBalance(Object invocationHandler) {
        log.info("当前实例是 {},开始增强LoadBalance功能", Target.class);
        Field dispatchField = ReflectionUtils.findField(invocationHandler.getClass(), "dispatch");
        dispatchField.setAccessible(true);
        Object val = ReflectionUtils.getField(dispatchField, invocationHandler);
        if (!(val instanceof Map)) {
            return;
        }
        Map<Method, InvocationHandlerFactory.MethodHandler> dispatch = (Map<Method, InvocationHandlerFactory.MethodHandler>) val;
        dispatch.values().stream().forEach(i -> {
            Field clientField = ReflectionUtils.findField(i.getClass(), "client");
            clientField.setAccessible(true);
            Object clientVal = ReflectionUtils.getField(clientField, i);
            if (clientVal instanceof LoadBalancerFeignClient && (!(clientVal instanceof VnetRouterLoadBalancerFeignClient))) {
                Client client = (Client) clientVal;
                client = new VnetRouterLoadBalancerFeignClient(client, lbClientFactory, clientFactory);
                ReflectionUtils.setField(clientField, i, client);
            }
        });
    }
}



