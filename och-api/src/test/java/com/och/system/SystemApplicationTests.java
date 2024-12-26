package com.och.system;

import com.och.api.OchApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

@SpringBootTest(classes = OchApiApplication.class)
class SystemApplicationTests {

    @Autowired
    private  ApplicationEventPublisher publisher;

    @Test
    void contextLoads() throws InterruptedException {


        /*ThreadUtil.execAsync(() ->{
            FlowData flowData = new FlowData();
            flowData.setAddress("111");
            flowData.setCallId(1L);
            flowData.setUniqueId("1111");
            publisher.publishEvent(new FlowEvent(1L,1,flowData));
        });


        ThreadUtil.execAsync(() ->{
            FlowData flowData = new FlowData();
            flowData.setAddress("2222");
            flowData.setCallId(2L);
            flowData.setUniqueId("222");
            publisher.publishEvent(new FlowEvent(1L,1,flowData));
        });

        ThreadUtil.execAsync(() ->{
            FlowData flowData = new FlowData();
            flowData.setAddress("3333");
            flowData.setCallId(3L);
            flowData.setUniqueId("3333");
            publisher.publishEvent(new FlowEvent(1L,1,flowData));
        });
        ThreadUtil.execAsync(() ->{
            FlowData flowData = new FlowData();
            flowData.setAddress("44444");
            flowData.setCallId(4L);
            flowData.setUniqueId("44444");
            publisher.publishEvent(new FlowEvent(1L,1,flowData));
        });
        Thread.sleep(10000000000000000L);*/
        //publisher.publishEvent(new FlowEvent(1L,2,46L,new FlowData()));
    }

}
