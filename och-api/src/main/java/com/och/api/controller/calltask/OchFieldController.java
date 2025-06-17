package com.och.api.controller.calltask;


import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.query.calltask.FieldAddQuery;
import com.och.system.domain.query.calltask.FieldQuery;
import com.och.system.domain.vo.calltask.FieldInfoVo;
import com.och.system.service.IOchFieldInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字段配置管理
 *
 * @author danmo
 * @date 2025/06/16 14:59
 */
@Tag(name = "字段配置管理")
@RestController
@RequestMapping("/call/task/v1/field")
public class OchFieldController extends BaseController {

    @Autowired
    private IOchFieldInfoService ochFieldInfoService;


    @Log(title = "新增字段", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:task:field:add')")
    @Operation(summary = "新增字段", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated FieldAddQuery query) {
        ochFieldInfoService.add(query);
        return success();
    }

    @Log(title = "修改字段", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:task:field:edit')")
    @Operation(summary = "修改字段", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated FieldAddQuery query) {
        query.setId(id);
        ochFieldInfoService.edit(query);
        return success();
    }

    @Log(title = "字段详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:task:field:get')")
    @Operation(summary = "字段详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<FieldInfoVo> get(@PathVariable("id") Long id) {
        return success(ochFieldInfoService.get(id));
    }


    @Log(title = "删除字段", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:task:field:delete')")
    @Operation(summary = "删除字段", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody FieldQuery query) {
        ochFieldInfoService.delete(query);
        return success();
    }

    @Log(title = "字段列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:task:field:page:list')")
    @Operation(summary = "字段列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<FieldInfoVo>> pageList(@RequestBody FieldQuery query) {
        List<FieldInfoVo> list = ochFieldInfoService.pageList(query);
        return success(new PageInfo<>(list));
    }

    @Log(title = "字段列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:task:field:list')")
    @Operation(summary = "字段列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ResResult<List<FieldInfoVo>> list(@RequestBody FieldQuery query) {
        List<FieldInfoVo> list = ochFieldInfoService.getList(query);
        return success(list);
    }

}
