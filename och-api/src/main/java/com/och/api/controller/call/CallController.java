package com.och.api.controller.call;

import com.github.pagehelper.PageInfo;
import com.och.api.service.ICallService;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.system.domain.query.call.CallQuery;
import com.och.system.domain.query.call.CallRecordQuery;
import com.och.system.domain.vo.call.CallRecordVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(description = "创建呼叫", method = "POST")
    @PostMapping("/create")
    public ResResult makeCall(@RequestBody CallQuery query){
        Long callId = iCallService.makeCall(query);
        return success(callId);
    }

    /**
     * 呼叫详情
     * @return
     */
    @Operation(description = "呼叫详情", method = "POST")
    @PostMapping("/get/{callId}")
    public ResResult<CallRecordVo> getCallInfo(@PathVariable("callId") Long callId){
        CallRecordVo callRecord = iCallService.getCallInfo(callId);
        return success(callRecord);
    }

    @Operation(description = "呼叫列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<CallRecordVo>> getCallPageList(@RequestBody CallRecordQuery query){
        List<CallRecordVo> recordList = iCallService.getCallPageList(query);
        PageInfo<CallRecordVo> pageInfo = new PageInfo<>(recordList);
        return success(pageInfo);
    }
}
