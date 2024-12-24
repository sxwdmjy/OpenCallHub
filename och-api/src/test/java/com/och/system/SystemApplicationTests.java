package com.och.system;

import com.och.api.OchApiApplication;
import com.och.ivr.event.FlowEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

@SpringBootTest(classes = OchApiApplication.class)
class SystemApplicationTests {

    @Autowired
    private  ApplicationEventPublisher publisher;

    @Test
    void contextLoads() {
        //publisher.publishEvent(new FlowEvent(1L,1,1L,"test"));
        publisher.publishEvent(new FlowEvent(1L,2,1L));
    }

}
