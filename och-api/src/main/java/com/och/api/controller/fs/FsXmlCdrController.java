package com.och.api.controller.fs;

import com.och.api.service.IFsXmlCdrService;
import com.och.common.exception.ParserException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author danmo
 * @date 2023年09月15日 17:33
 */
@Slf4j
@Tag(name = "cdr管理")
@RestController
@RequestMapping("/fs/cdr")
public class FsXmlCdrController {

    @Autowired
    private IFsXmlCdrService iFsXmlCdrService;

    @Operation(summary = "cdr请求", method = "POST")
    @PostMapping(value = "/api",produces = MediaType.APPLICATION_XML_VALUE)
    public void cdr(@RequestBody String reqText) throws ParserException {
        iFsXmlCdrService.cdrHandler(reqText);
    }

}
