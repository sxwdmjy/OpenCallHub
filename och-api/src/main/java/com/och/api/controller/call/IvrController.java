package com.och.api.controller.call;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.base.BaseController;
import com.och.common.enums.BusinessTypeEnum;
import com.och.ivr.domain.IvrFlow;
import com.och.ivr.service.IIvrFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * IVR 管理控制器
 * <p>适配新的 IVR 流程服务接口，保留旧版路由，便于前端平滑迁移。</p>
 *
 * @author danmo
 * @date 2024年12月23日 13:37
 */
@Tag(name = "IVR管理")
@RestController
@RequestMapping("/call/v1/ivr")
@RequiredArgsConstructor
public class IvrController extends BaseController {
    private final IIvrFlowService iFlowInfoService;

    @Log(title = "新增IVR流程", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:ivr:add')")
    @Operation(summary = "新增IVR流程", method = "POST")
    @PostMapping("/add")
    public ApiResponse add(@RequestBody @Validated IvrFlow flow) {
        iFlowInfoService.create(flow);
        return success();
    }

    @Log(title = "修改IVR流程", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:ivr:edit')")
    @Operation(summary = "修改IVR流程", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse edit(@PathVariable("id") Long id, @RequestBody @Validated IvrFlow flow) {
        iFlowInfoService.update(id, flow);
        return success();
    }

    @Log(title = "删除IVR流程", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:ivr:delete')")
    @Operation(summary = "删除IVR流程", method = "POST")
    @PostMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable("id") Long id) {
        iFlowInfoService.delete(id);
        return success();
    }

    @Log(title = "查询IVR流程", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:ivr:get')")
    @Operation(summary = "查询IVR流程", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<IvrFlow> get(@PathVariable("id") Long id) {
        return success(iFlowInfoService.getById(id));
    }

    @Log(title = "查询IVR流程列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:ivr:page:list')")
    @Operation(summary = "查询IVR流程列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<IvrFlow>> pageList(@RequestBody IvrFlow query) {
        List<IvrFlow> list = iFlowInfoService.list();
        return success(new PageInfo<>(list));
    }

    @Log(title = "发布流程", businessType = BusinessTypeEnum.OTHER)
    @PreAuthorize("@authz.hasPerm('call:ivr:publish')")
    @Operation(summary = "发布流程", method = "POST")
    @PostMapping("/publish/{id}")
    public ApiResponse publish(@PathVariable("id") Long id) {
        iFlowInfoService.publish(id);
        return success();
    }

    @Log(title = "下线流程", businessType = BusinessTypeEnum.OTHER)
    @PreAuthorize("@authz.hasPerm('call:ivr:offline')")
    @Operation(summary = "下线流程", method = "POST")
    @PostMapping("/offline/{id}")
    public ApiResponse offline(@PathVariable("id") Long id) {
        iFlowInfoService.offline(id);
        return success();
    }
}
