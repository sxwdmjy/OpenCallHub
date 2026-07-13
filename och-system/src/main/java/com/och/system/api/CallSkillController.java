package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.query.skill.CallSkillAddQuery;
import com.och.system.domain.query.skill.CallSkillQuery;
import com.och.system.domain.vo.skill.CallSkillVo;
import com.och.system.service.ICallSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 技能管理
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "技能管理")
@RestController
@RequestMapping("call/v1/skill")
@RequiredArgsConstructor
public class CallSkillController {

    private final ICallSkillService callSkillService;

    @Log(title = "新增技能", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:skill:add')")
    @Operation(summary = "新增技能", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated CallSkillAddQuery query) {
        callSkillService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改技能", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:skill:edit')")
    @Operation(summary = "修改技能", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable("id") Long id, @RequestBody @Validated CallSkillAddQuery query) {
        query.setId(id);
        callSkillService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "删除技能", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:skill:delete')")
    @Operation(summary = "删除技能", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody CallSkillQuery query) {
        callSkillService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "技能详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:skill:get')")
    @Operation(summary = "技能详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<CallSkillVo> getDetail(@PathVariable("id") Long id) {
        CallSkillVo skillVo = callSkillService.getDetail(id);
        return ApiResponse.success(skillVo);
    }

    @Log(title = "技能列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:skill:page:list')")
    @Operation(summary = "技能列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<CallSkillVo>> pageList(@RequestBody CallSkillQuery query) {
        PageInfo<CallSkillVo> list = callSkillService.pageList(query);
        return ApiResponse.success(list);
    }

    @Operation(summary = "技能列表", method = "POST")
    @PostMapping("/list")
    public ApiResponse<List<CallSkillVo>> getList(@RequestBody CallSkillQuery query) {
        List<CallSkillVo> list = callSkillService.getList(query);
        return ApiResponse.success(list);
    }

    @Operation(summary = "通过ID列表获取技能列表", method = "POST")
    @PostMapping("/listByIds")
    public ApiResponse<List<CallSkillVo>> getListByIds(@RequestBody CallSkillQuery query) {
        List<CallSkillVo> list = callSkillService.getListByIds(query);
        return ApiResponse.success(list);
    }
}
