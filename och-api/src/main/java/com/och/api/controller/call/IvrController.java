package com.och.api.controller.call;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.ivr.domain.query.FlowInfoAddQuery;
import com.och.ivr.domain.query.FlowInfoQuery;
import com.och.ivr.domain.vo.FlowInfoListVo;
import com.och.ivr.domain.vo.FlowInfoVo;
import com.och.ivr.service.IFlowInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author danmo
 * @date 2024年12月23日 13:37
 */
@Tag(name = "IVR管理")
@RestController
@RequestMapping("/call/v1/ivr")
public class IvrController extends BaseController {

    @Autowired
    private IFlowInfoService iFlowInfoService;

    @Log(title = "新增IVR流程", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:ivr:add')")
    @Operation(summary = "新增IVR流程", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated FlowInfoAddQuery query) {
        iFlowInfoService.add(query);
        return success();
    }

    @Log(title = "修改IVR流程", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:ivr:edit')")
    @Operation(summary = "修改IVR流程", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated FlowInfoAddQuery query) {
        query.setId(id);
        iFlowInfoService.edit(query);
        return success();
    }

    @Log(title = "删除IVR流程", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:ivr:delete')")
    @Operation(summary = "删除IVR流程", method = "POST")
    @PostMapping("/delete/{id}")
    public ResResult delete(@PathVariable("id") Long id) {
        iFlowInfoService.delete(id);
        return success();
    }

    @Log(title = "查询IVR流程", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:ivr:get')")
    @Operation(summary = "查询IVR流程", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<FlowInfoVo> get(@PathVariable("id") Long id) {
        FlowInfoVo info = iFlowInfoService.getInfo(id);
        return success(info);
    }


    @Log(title = "查询IVR流程列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:ivr:page:list')")
    @Operation(summary = "查询IVR流程列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<FlowInfoListVo>> pageList(@RequestBody FlowInfoQuery query) {
        List<FlowInfoListVo> list = iFlowInfoService.pageList(query);
        PageInfo<FlowInfoListVo> pageInfo = new PageInfo<>(list);
        return success(pageInfo);
    }

    @Log(title = "发布流程", businessType = BusinessTypeEnum.OTHER)
    @PreAuthorize("@authz.hasPerm('call:ivr:publish')")
    @Operation(summary = "发布流程", method = "POST")
    @PostMapping("/publish/{id}")
    public ResResult publish(@PathVariable("id") Long id) {
        iFlowInfoService.publish(id);
        return success();
    }

    @Log(title = "下线流程" , businessType = BusinessTypeEnum.OTHER)
    @PreAuthorize("@authz.hasPerm('call:ivr:offline')")
    @Operation(summary = "下线流程", method = "POST")
    @PostMapping("/offline/{id}")
    public ResResult offline(@PathVariable("id") Long id) {
        iFlowInfoService.offline(id);
        return success();
    }

}
