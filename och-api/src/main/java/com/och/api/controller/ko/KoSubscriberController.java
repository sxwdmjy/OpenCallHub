package com.och.api.controller.ko;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.KoSubscriber;
import com.och.system.domain.query.subsriber.KoSubscriberAddQuery;
import com.och.system.domain.query.subsriber.KoSubscriberBatchAddQuery;
import com.och.system.domain.query.subsriber.KoSubscriberQuery;
import com.och.system.domain.query.subsriber.KoSubscriberUpdateQuery;
import com.och.system.domain.vo.sip.KoSubscriberVo;
import com.och.system.domain.vo.sip.SipSimpleVo;
import com.och.system.service.IKoSubscriberService;
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
public class KoSubscriberController extends BaseController {

    @Autowired
    private IKoSubscriberService subscriberService;

    @Log(title = "新增SIP号码", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:add')")
    @Operation(summary = "新增SIP号码", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated KoSubscriberAddQuery query) {
        subscriberService.add(query);
        return success();
    }

    @Log(title = "批量新增SIP号码", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:batch:add')")
    @Operation(summary = "批量新增SIP号码", method = "POST")
    @PostMapping("/batch/add")
    public ResResult batchAdd(@RequestBody @Validated KoSubscriberBatchAddQuery query) {
        subscriberService.batchAdd(query);
        return success();
    }

    @Log(title = "修改SIP号码", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:subscriber:edit')")
    @Operation(summary = "修改SIP号码", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Integer id, @RequestBody @Validated KoSubscriberUpdateQuery query) {
        query.setId(id);
        subscriberService.edit(query);
        return success();
    }

    @Log(title = "SIP号码详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:get')")
    @Operation(summary = "SIP号码详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<KoSubscriber> get(@PathVariable("id") Integer id) {
        return ResResult.success(subscriberService.getDetail(id));
    }

    @Log(title = "删除SIP号码", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:subscriber:del')")
    @Operation(summary = "删除SIP号码", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody KoSubscriberQuery query) {
        subscriberService.delete(query);
        return success();
    }

    @Log(title = "SIP号码列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:page:list')")
    @Operation(summary = "SIP号码列表", method = "POST")
    @PostMapping("/list")
    public ResResult<PageInfo<KoSubscriberVo>> list(@RequestBody KoSubscriberQuery query) {
        List<KoSubscriberVo> list = subscriberService.getPageList(query);
        PageInfo<KoSubscriberVo> pageInfo = new PageInfo<>(list);
        return success(pageInfo);
    }

    @Log(title = "SIP号码下拉列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:select:list')")
    @Operation(summary = "SIP号码下拉列表", method = "POST")
    @PostMapping("/select/list")
    public ResResult<List<SipSimpleVo>> selectList() {
        List<SipSimpleVo> list = subscriberService.selectList();
        return success(list);
    }


}
