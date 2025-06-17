package com.och.system;

import cn.hutool.core.util.IdUtil;
import com.och.ai.service.IDashScopeService;
import com.och.api.OchApiApplication;
import com.och.system.domain.query.calltask.DataSourceContactQuery;
import com.och.system.domain.vo.calltask.DataSourcesContactVo;
import com.och.system.service.IOchDataSourcesContactService;
import com.och.system.service.IOchDataSourcesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@SpringBootTest(classes = OchApiApplication.class)
class SystemApplicationTests {

    @Autowired
    private ApplicationEventPublisher publisher;

    /*@Autowired
    private IDeepSeekService deepSeekService;*/

    @Autowired
    private IDashScopeService dashScopeService;

    @Test
    void contextLoads() throws InterruptedException {
        String chatId = IdUtil.fastSimpleUUID();
        String chat = dashScopeService.chat(chatId,"你好，介绍下你自己！");
        System.out.println(chat);
        String str = dashScopeService.chat(chatId,"帮我写首登高的五言绝句");
        System.out.println(str);
        String str1 = dashScopeService.chat(chatId,"复述上述问题");
        System.out.println(str1);
        String str2 = dashScopeService.chat(chatId,"是的");
        System.out.println(str2);
    }

    @Autowired
    private IOchDataSourcesContactService ochDataSourcesContactService;
    @Test
    void test() {
        DataSourceContactQuery query = new DataSourceContactQuery();
        query.setPhone("13262531293");
        query.setSourceId(1L);
        List<DataSourcesContactVo> contactList = ochDataSourcesContactService.getContactList(query);
        System.out.println(contactList);
    }

}
