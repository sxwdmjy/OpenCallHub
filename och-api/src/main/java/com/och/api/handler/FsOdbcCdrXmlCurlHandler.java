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
import com.och.common.xmlcurl.odbccdr.OdbcCdrConfiguration;
import com.och.system.domain.entity.FsModules;
import com.och.common.domain.FsXmlCurl;
import com.och.system.domain.query.modules.FsModulesQuery;
import com.och.system.service.IFsModulesService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * odbc_cdr处理类
 *
 * @author danmo
 * @date 2023/09/13 22:02
 **/
@XmlCurlEventName(value = SectionNames.ODBC_CDR)
@Slf4j
@Component
public class FsOdbcCdrXmlCurlHandler implements FsXmlCurlEventStrategy {

    @Resource
    private IFsModulesService iFsModulesService;

    @Override
    public String eventHandle(FsXmlCurl fsXmlCurl) {
        String xml = "";
        try {
            xml += getContext(fsXmlCurl.getKeyValue());
        } catch (JsonProcessingException e) {
          log.error("Failed to process XML ODBC_CDR configuration for key: {}", fsXmlCurl.getKeyValue(), e);
        }
        log.info("dialplanHandle xml curl : {}, {}", JSON.toJSONString(fsXmlCurl, true), xml);
        return xml;
    }

    private String getContext(String name) throws JsonProcessingException {
        OdbcCdrConfiguration configuration = getConfiguration(name);
        return configuration.toXmlString();
    }



    private OdbcCdrConfiguration getConfiguration(String name) throws JsonProcessingException {
        OdbcCdrConfiguration cdrConfiguration = new OdbcCdrConfiguration();
        FsModulesQuery query = new FsModulesQuery();
        query.setName(name);
        List<FsModules> fsModules = iFsModulesService.getList(query);
        if(CollectionUtil.isNotEmpty(fsModules)){
            FsModules fsModule = fsModules.get(0);
            if ("json".equals(fsModule.getType())) {
                String content = fsModule.getContent();
                cdrConfiguration = JSONObject.parseObject(content, OdbcCdrConfiguration.class);
            }
            if ("xml".equals(fsModule.getType())) {
                String content = fsModule.getContent();
                String valueAsString = new ObjectMapper().writeValueAsString(new XmlMapper().readTree(content));
                cdrConfiguration = JSONObject.parseObject(valueAsString, OdbcCdrConfiguration.class);
            }
        }

        return cdrConfiguration;
    }


}
