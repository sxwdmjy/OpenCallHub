package com.och.api.controller;

import com.och.api.service.ICallService;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.system.domain.query.call.CallQuery;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 拨打接口
 * @author danmo
 * @date 2024年08月28日 17:38
 */
@Tag(name = "呼叫接口管理")
@RequestMapping("/v1/call")
@RestController
public class CallController extends BaseController {

    @Autowired
    private ICallService iCallService;

    /**
     * 创建呼叫
     * @return
     */
    @PostMapping("/create")
    public ResResult makeCall(@RequestBody CallQuery query){
        Long callId = iCallService.makeCall(query);
        return success(callId);
    }
}
