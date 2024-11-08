package com.och.api.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.och.api.factory.FsXmlCurlEventStrategy;
import com.och.common.annotation.XmlCurlEventName;
import com.och.common.constant.SectionNames;
import com.och.common.xmlcurl.dialplan.Context;
import com.och.common.xmlcurl.dialplan.Extension;
import com.och.system.domain.entity.FsDialplan;
import com.och.common.domain.FsXmlCurl;
import com.och.system.domain.query.dialplan.FsDialplanQuery;
import com.och.system.domain.vo.dialplan.FsDialplanVo;
import com.och.system.service.IFsDialplanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * 拨号计划处理类
 *
 * @author danmo
 * @date 2023/09/13 22:02
 **/
@XmlCurlEventName(value = SectionNames.DIALPLAN)
@Slf4j
@Component
public class FsDialplanXmlCurlHandler implements FsXmlCurlEventStrategy {


    @Resource
    private IFsDialplanService iFsDialplanService;

    @Override
    public String eventHandle(FsXmlCurl fsXmlCurl) {
        String xml = "";
        try {
            xml += getContext("public");
            xml += getContext("default");
        } catch (JsonProcessingException e) {
            log.error("FS获取动态拨号计划异常 msg:{}",e.getMessage(),e);
        }
        log.info("dialplanHandle xml curl : {}, {}", JSON.toJSONString(fsXmlCurl, true), xml);
        return xml;
    }

    private String getContext(String name) throws JsonProcessingException {
        Context context = new Context();
        context.setName(name);
        FsDialplanQuery query = new FsDialplanQuery();
        query.setContextName(name);
        List<FsDialplanVo> dialplanList = iFsDialplanService.getList(query);
        if (CollectionUtil.isNotEmpty(dialplanList)) {
            List<Extension> extensionList = new LinkedList<>();
            for (FsDialplanVo fsDialplan : dialplanList) {
                Extension extension = new Extension();
                if ("json".equals(fsDialplan.getType())) {
                    String content = fsDialplan.getContent();
                    extension = JSONObject.parseObject(content, Extension.class);
                }
                if ("xml".equals(fsDialplan.getType())) {
                    String content = fsDialplan.getContent();
                    String entries = new ObjectMapper().writeValueAsString(new XmlMapper().readTree(content));
                    extension = JSONObject.parseObject(entries, Extension.class);
                }
                extension.setName(fsDialplan.getName());
                extensionList.add(extension);
            }
            context.setExtension(extensionList);
        }
        return context.toXmlString();
    }
}
