package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.FsSipGateway;
import com.och.system.domain.query.fssip.FsSipGatewayAddQuery;
import com.och.system.domain.query.fssip.FsSipGatewayQuery;
import com.och.system.service.IFsSipGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FreeSWITCH 网关管理
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "fs网关管理")
@RestController
@RequestMapping("/fs/v1/gateway")
@RequiredArgsConstructor
public class FsGatewayController {

    private final IFsSipGatewayService fsSipGatewayService;

    @Log(title = "新增网关", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:fs:gateway:add')")
    @Operation(summary = "新增网关", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated FsSipGatewayAddQuery query) {
        fsSipGatewayService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改网关", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:fs:gateway:edit')")
    @Operation(summary = "修改网关", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable("id") Long id, @RequestBody @Validated FsSipGatewayAddQuery query) {
        query.setId(id);
        fsSipGatewayService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "网关详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:gateway:get')")
    @Operation(summary = "网关详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<FsSipGateway> get(@PathVariable("id") Long id) {
        return ApiResponse.success(fsSipGatewayService.getDetail(id));
    }

    @Log(title = "删除网关", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:fs:gateway:delete')")
    @Operation(summary = "删除网关", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody FsSipGatewayQuery query) {
        fsSipGatewayService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "网关列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:gateway:page:list')")
    @Operation(summary = "网关列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<FsSipGateway>> pageList(@RequestBody FsSipGatewayQuery query) {
        List<FsSipGateway> list = fsSipGatewayService.getPageList(query);
        PageInfo<FsSipGateway> pageInfo = new PageInfo<>(list);
        return ApiResponse.success(pageInfo);
    }

    @Log(title = "网关列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:fs:gateway:list')")
    @Operation(summary = "网关列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ApiResponse<List<FsSipGateway>> list(@RequestBody FsSipGatewayQuery query) {
        List<FsSipGateway> list = fsSipGatewayService.getList(query);
        return ApiResponse.success(list);
    }
}
