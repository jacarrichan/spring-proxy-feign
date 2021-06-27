package com.jacarrichan.demo.discovery.client.configure;

import com.jacarrichan.demo.discovery.client.DemoServiceMockResponseTest;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.DataCenterInfo;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInfo;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.anyString;

/**
 * @Author by AA2668 on 2021/6/24.
 */
@Slf4j
@Configuration
public class MockContextConfigure {
    @Bean
    EurekaClient eurekaClient() {
        ApplicationInfoManager applicationInfoManager = Mockito.mock(ApplicationInfoManager.class);
        EurekaClientConfig config = Mockito.mock(EurekaClientConfig.class);
        System.err.println(config);
        Mockito.doReturn(new String[]{}).when(config).getAvailabilityZones(anyString());
        AbstractDiscoveryClientOptionalArgs args = Mockito.mock(AbstractDiscoveryClientOptionalArgs.class);
        //先试用构造器给静态属性赋值
        EurekaClient eurekaClientCopy = new DiscoveryClient(applicationInfoManager, config, args);
        log.info("eurekaClientCopy:{}", eurekaClientCopy);
        //然后mock
        EurekaClient eurekaClient = Mockito.mock(CloudEurekaClient.class);
        InstanceInfo instance = Mockito.mock(InstanceInfo.class);
        Mockito.doReturn(InstanceInfo.InstanceStatus.UP).when(instance).getStatus();
        DataCenterInfo dataCenterInfo = Mockito.mock(MyDataCenterInfo.class);
        Mockito.doReturn(DataCenterInfo.Name.MyOwn).when(dataCenterInfo).getName();
        Mockito.doReturn("localhost").when(instance).getHostName();
        Mockito.doReturn(DemoServiceMockResponseTest.port).when(instance).getPort();
        Mockito.doReturn("localhost").when(instance).getIPAddr();
        Mockito.doReturn(dataCenterInfo).when(instance).getDataCenterInfo();
        Mockito.doReturn(InstanceInfo.InstanceStatus.UP).when(instance).getStatus();
        Map<String, String> metaMap = new HashMap<>();
        metaMap.put("group", Joiner.on(",").join(Lists.newArrayList("INTERNAL"," CCD", "EXTERNAL")));
        Mockito.doReturn(metaMap).when(instance).getMetadata();
        List<InstanceInfo> instanceList = Lists.newArrayList(instance);
        Mockito.doReturn(instanceList).when(eurekaClient).getInstancesByVipAddress(anyString(), Mockito.anyBoolean(), anyString());
        Mockito.doReturn(instanceList).when(eurekaClient).getInstancesByVipAddress("demo-zuul", false);

        return eurekaClient;
    }
}
