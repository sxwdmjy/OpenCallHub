package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SIP号码管理
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "SIP号码管理")
@RestController
@RequestMapping("/ko/v1/subscriber")
@RequiredArgsConstructor
public class KoSubscriberController {

    private final IKoSubscriberService subscriberService;

    @Log(title = "新增SIP号码", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:add')")
    @Operation(summary = "新增SIP号码", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated KoSubscriberAddQuery query) {
        subscriberService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "批量新增SIP号码", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:batch:add')")
    @Operation(summary = "批量新增SIP号码", method = "POST")
    @PostMapping("/batch/add")
    public ApiResponse<Void> batchAdd(@RequestBody @Validated KoSubscriberBatchAddQuery query) {
        subscriberService.batchAdd(query);
        return ApiResponse.success();
    }

    @Log(title = "修改SIP号码", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:subscriber:edit')")
    @Operation(summary = "修改SIP号码", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable("id") Integer id, @RequestBody @Validated KoSubscriberUpdateQuery query) {
        query.setId(id);
        subscriberService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "SIP号码详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:get')")
    @Operation(summary = "SIP号码详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<KoSubscriber> get(@PathVariable("id") Integer id) {
        return ApiResponse.success(subscriberService.getDetail(id));
    }

    @Log(title = "删除SIP号码", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:subscriber:del')")
    @Operation(summary = "删除SIP号码", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody KoSubscriberQuery query) {
        subscriberService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "SIP号码列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:page:list')")
    @Operation(summary = "SIP号码列表", method = "POST")
    @PostMapping("/list")
    public ApiResponse<PageInfo<KoSubscriberVo>> list(@RequestBody KoSubscriberQuery query) {
        List<KoSubscriberVo> list = subscriberService.getPageList(query);
        PageInfo<KoSubscriberVo> pageInfo = new PageInfo<>(list);
        return ApiResponse.success(pageInfo);
    }

    @Log(title = "SIP号码下拉列表", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:subscriber:select:list')")
    @Operation(summary = "SIP号码下拉列表", method = "POST")
    @PostMapping("/select/list")
    public ApiResponse<List<SipSimpleVo>> selectList() {
        List<SipSimpleVo> list = subscriberService.selectList();
        return ApiResponse.success(list);
    }
}
