package com.och.api.controller.fs;

import com.alibaba.fastjson.JSONObject;
import com.och.api.service.IFsXmlCurlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

;

/**
 * @author danmo
 * @date 2023年09月14日 13:33
 */
@Slf4j
@Tag(name = "curl管理")
@RestController
@RequestMapping("/fs/curl")
public class FsXmlCurlController {

    @Autowired
    private IFsXmlCurlService iFsXmlCurlService;

    @Operation(summary = "POST请求", method = "POST")
    @PostMapping(value = "/api", produces = {MediaType.TEXT_XML_VALUE})
    public String apiPost(HttpServletRequest request) {
        log.info("xmlCurl POST请求 {}", JSONObject.toJSONString(request.getParameterMap()));
        return iFsXmlCurlService.xmlHandle(request);
    }

    @Operation(summary = "GET请求", method = "GET")
    @GetMapping(value = "/api", produces = {MediaType.TEXT_XML_VALUE})
    public String apiGet(HttpServletRequest request) {
        log.info("xmlCurl GET请求 {}", JSONObject.toJSONString(request.getParameterMap()));
        return iFsXmlCurlService.xmlHandle(request);
    }

}
