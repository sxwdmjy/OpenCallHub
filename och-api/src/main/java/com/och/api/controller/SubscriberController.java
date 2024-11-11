package com.och.api.controller;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.Subscriber;
import com.och.system.domain.query.subsriber.SubscriberAddQuery;
import com.och.system.domain.query.subsriber.SubscriberBatchAddQuery;
import com.och.system.domain.query.subsriber.SubscriberQuery;
import com.och.system.domain.query.subsriber.SubscriberUpdateQuery;
import com.och.system.domain.vo.sip.SubscriberVo;
import com.och.system.service.ISubscriberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author danmo
 * @date 2024年07月28日 13:37
 */
@Tag(name = "SIP号码管理")
@RestController
@RequestMapping("/system/v1/sip")
public class SubscriberController extends BaseController {

    @Autowired
    private ISubscriberService subscriberService;

    @Log(title = "新增SIP号码", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:add')")
    @Operation(summary = "新增SIP号码", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated SubscriberAddQuery query) {
        subscriberService.add(query);
        return success();
    }

    @Log(title = "批量新增SIP号码", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:batch:add')")
    @Operation(summary = "批量新增SIP号码", method = "POST")
    @PostMapping("/batch/add")
    public ResResult batchAdd(@RequestBody @Validated SubscriberBatchAddQuery query) {
        subscriberService.batchAdd(query);
        return success();
    }

    @Log(title = "修改SIP号码", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:subscriber:edit')")
    @Operation(summary = "修改SIP号码", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Integer id, @RequestBody @Validated SubscriberUpdateQuery query) {
        query.setId(id);
        subscriberService.edit(query);
        return success();
    }

    @Log(title = "SIP号码详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:get')")
    @Operation(summary = "SIP号码详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<Subscriber> get(@PathVariable("id") Integer id) {
        return ResResult.success(subscriberService.getDetail(id));
    }

    @Log(title = "删除SIP号码", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:subscriber:del')")
    @Operation(summary = "删除SIP号码", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody SubscriberQuery query) {
        subscriberService.delete(query);
        return success();
    }

    @Log(title = "SIP号码列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:page:list')")
    @Operation(summary = "SIP号码列表", method = "POST")
    @PostMapping("/list")
    public ResResult<PageInfo<SubscriberVo>> list(@RequestBody SubscriberQuery query) {
        List<SubscriberVo> list = subscriberService.getPageList(query);
        PageInfo<SubscriberVo> pageInfo = new PageInfo<>(list);
        return success(pageInfo);
    }

}
