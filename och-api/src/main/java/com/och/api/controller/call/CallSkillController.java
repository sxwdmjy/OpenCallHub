package com.och.api.controller.call;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.query.skill.CallSkillAddQuery;
import com.och.system.domain.query.skill.CallSkillQuery;
import com.och.system.domain.vo.skill.CallSkillVo;
import com.och.system.service.ICallSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author danmo
 * @date 2023-10-31 13:49
 **/
@Tag(name = "技能管理")
@RestController
@RequestMapping("call/v1/skill")
public class CallSkillController extends BaseController {

    @Autowired
    private ICallSkillService iCallSkillService;

    @Log(title = "新增技能", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:skill:add')")
    @Operation(summary = "新增技能", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated CallSkillAddQuery query) {
        iCallSkillService.add(query);
        return success();
    }

    @Log(title = "修改技能", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:skill:edit')")
    @Operation(summary = "修改技能", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated CallSkillAddQuery query) {
        query.setId(id);
        iCallSkillService.edit(query);
        return success();
    }

    @Log(title = "删除技能", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:skill:edit')")
    @Operation(summary = "删除技能", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CallSkillQuery query) {
        iCallSkillService.delete(query);
        return success();
    }

    @Log(title = "技能详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:skill:get')")
    @Operation(summary = "技能详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<CallSkillVo> getDetail(@PathVariable("id") Long id) {
        CallSkillVo skillVo = iCallSkillService.getDetail(id);
        return success(skillVo);
    }

    @Log(title = "技能列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:skill:page:list')")
    @Operation(summary = "技能列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<CallSkillVo>> pageList(@RequestBody CallSkillQuery query) {
        PageInfo<CallSkillVo> list = iCallSkillService.pageList(query);
        return success(list);
    }

    @Operation(summary = "技能列表", method = "POST")
    @PostMapping("/list")
    public ResResult<List<CallSkillVo>> getList(@RequestBody CallSkillQuery query) {
        List<CallSkillVo> list = iCallSkillService.getList(query);
        return success(list);
    }

    @Operation(summary = "通过ID列表获取技能列表", method = "POST")
    @PostMapping("/listByIds")
    public ResResult<List<CallSkillVo>> getListByIds(@RequestBody CallSkillQuery query) {
        List<CallSkillVo> list = iCallSkillService.getListByIds(query);
        return success(list);
    }
}
