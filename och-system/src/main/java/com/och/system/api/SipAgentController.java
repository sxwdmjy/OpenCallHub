package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.system.domain.query.agent.SipAgentAddQuery;
import com.och.system.domain.query.agent.SipAgentQuery;
import com.och.system.domain.vo.agent.SipAgentVo;
import com.och.system.service.ISipAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 坐席管理
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "坐席管理")
@RestController
@RequestMapping("/system/v1/agent")
@RequiredArgsConstructor
public class SipAgentController {

    private final ISipAgentService sipAgentService;

    @Log(title = "新增坐席", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:agent:add')")
    @Operation(summary = "新增坐席", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated SipAgentAddQuery query) {
        sipAgentService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改坐席", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:agent:edit')")
    @Operation(summary = "修改坐席", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable("id") Long id, @RequestBody @Validated SipAgentAddQuery query) {
        query.setId(id);
        sipAgentService.update(query);
        return ApiResponse.success();
    }

    @Log(title = "删除坐席", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:agent:delete')")
    @Operation(summary = "删除坐席", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody SipAgentQuery query) {
        sipAgentService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "坐席详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:agent:get')")
    @Operation(summary = "坐席详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<SipAgentVo> getDetail(@PathVariable("id") Long id) {
        SipAgentVo detail = sipAgentService.getDetail(id);
        return ApiResponse.success(detail);
    }

    @Log(title = "坐席列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:agent:page:list')")
    @Operation(summary = "坐席列表", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<SipAgentVo>> getPageList(@RequestBody SipAgentQuery query) {
        PageInfo<SipAgentVo> list = sipAgentService.getPageList(query);
        return ApiResponse.success(list);
    }

    @Operation(summary = "根据条件查询坐席", method = "POST")
    @PostMapping("/getInfoByQuery")
    public ApiResponse<List<SipAgentVo>> getInfoByQuery(@RequestBody SipAgentQuery query) {
        List<SipAgentVo> sipAgentVoList = sipAgentService.getInfoByQuery(query);
        return ApiResponse.success(sipAgentVoList);
    }

    @Operation(summary = "根据sip号码查询坐席", method = "GET")
    @GetMapping("/getInfoByAgent/{agentNum}")
    public ApiResponse<SipAgentVo> getInfoByAgent(@PathVariable("agentNum") String agentNum) {
        SipAgentVo sipAgentVo = sipAgentService.getInfoByAgent(agentNum);
        return ApiResponse.success(sipAgentVo);
    }

    @Operation(summary = "坐席状态变更", method = "POST")
    @PostMapping("/update/status/{id}")
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @RequestBody SipAgentQuery query) {
        query.setId(id);
        if (query.getStatus() == null) {
            throw new BizException(ErrorCode.BIZ_VALIDATION_ERROR, "状态不能为空");
        }
        sipAgentService.updateStatus(query.getId(), query.getStatus());
        return ApiResponse.success();
    }
}
