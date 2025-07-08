package com.och.api.controller.sys;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.CorpInfo;
import com.och.system.domain.query.corp.CorpAddQuery;
import com.och.system.domain.query.corp.CorpQuery;
import com.och.system.service.ICorpInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业管理
 *
 * @author danmo
 * @date 2023年07月06日 23:02
 */
@Tag(name = "企业管理")
@RestController
@RequestMapping("/system/v1/corp")
public class CorpController extends BaseController {

    @Autowired
    private ICorpInfoService iCorpInfoService;

    @Log(title = "新增企业", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:corp:add')")
    @Operation(summary = "新增企业", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated CorpAddQuery query) {
        iCorpInfoService.add(query);
        return success();
    }

    @Log(title = "修改企业信息", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:corp:edit')")
    @Operation(summary = "修改企业信息", method = "POST")
    @PostMapping("/edit/{corpId}")
    public ResResult edit(@PathVariable("corpId") Integer corpId, @RequestBody @Validated CorpAddQuery query) {
        query.setCorpId(corpId);
        iCorpInfoService.edit(query);
        return success();
    }

    @Operation(summary = "企业详情", method = "POST")
    @PostMapping("/get/{corpId}")
    public ResResult<CorpInfo> get(@PathVariable("corpId") Integer corpId) {
        return success(iCorpInfoService.getById(corpId));
    }

    @Log(title = "删除企业", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:corp:del')")
    @Operation(summary = "删除企业", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody CorpQuery query) {
        iCorpInfoService.delete(query);
        return success();
    }

    @Log(title = "企业列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:corp:page:list')")
    @Operation(summary = "企业列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<CorpInfo>> list(@RequestBody CorpQuery query) {
        List<CorpInfo> pageList = iCorpInfoService.getPageList(query);
        PageInfo<CorpInfo> pageInfo = PageInfo.of(pageList);
        return success(pageInfo);
    }

    @Operation(summary = "通过code获取企业详情", method = "POST")
    @PostMapping("/getByCode")
    public ResResult<CorpInfo> getByCode(@RequestBody CorpQuery query) {
        return success(iCorpInfoService.getByCode(query.getCorpCode()));
    }
}
