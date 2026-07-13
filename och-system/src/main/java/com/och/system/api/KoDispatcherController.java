package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.KoDispatcher;
import com.och.system.domain.query.dispatcher.KoDispatcherAddQuery;
import com.och.system.domain.query.dispatcher.KoDispatcherQuery;
import com.och.system.service.IKoDispatcherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * KO负载管理
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "KO负载管理")
@RestController
@RequestMapping("/ko/v1/dispatcher")
@RequiredArgsConstructor
public class KoDispatcherController {

    private final IKoDispatcherService dispatcherService;

    @Log(title = "新增负载", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:dispatcher:add')")
    @Operation(summary = "新增负载", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated KoDispatcherAddQuery query) {
        dispatcherService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改负载", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:dispatcher:edit')")
    @Operation(summary = "修改负载", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable("id") Integer id, @RequestBody @Validated KoDispatcherAddQuery query) {
        query.setId(id);
        dispatcherService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "负载详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:dispatcher:get')")
    @Operation(summary = "负载详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<KoDispatcher> get(@PathVariable("id") Integer id) {
        return ApiResponse.success(dispatcherService.getDetail(id));
    }

    @Log(title = "删除负载", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:dispatcher:del')")
    @Operation(summary = "删除负载", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody KoDispatcherQuery query) {
        dispatcherService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "负载列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:dispatcher:page:list')")
    @Operation(summary = "负载列表", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<KoDispatcher>> list(@RequestBody KoDispatcherQuery query) {
        List<KoDispatcher> list = dispatcherService.getPageList(query);
        PageInfo<KoDispatcher> pageInfo = new PageInfo<>(list);
        return ApiResponse.success(pageInfo);
    }
}
