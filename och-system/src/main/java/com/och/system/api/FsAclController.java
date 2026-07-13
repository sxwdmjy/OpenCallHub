package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.entity.FsAcl;
import com.och.system.domain.query.acl.FsAclAddQuery;
import com.och.system.domain.query.acl.FsAclNodeAddQuery;
import com.och.system.domain.query.acl.FsAclQuery;
import com.och.system.domain.vo.acl.FsAclVo;
import com.och.system.service.IFsAclService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FreeSWITCH 访问控制管理
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "fs访问控制管理")
@RestController
@RequestMapping("/fs/v1/acl")
@RequiredArgsConstructor
public class FsAclController {

    private final IFsAclService fsAclService;

    @Log(title = "新增acl规则表", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:acl:table:add')")
    @Operation(summary = "新增acl规则表", method = "POST")
    @PostMapping("/list/add")
    public ApiResponse<Void> addList(@RequestBody @Validated FsAclAddQuery query) {
        fsAclService.addList(query);
        return ApiResponse.success();
    }

    @Log(title = "新增acl规则", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('system:acl:node:add')")
    @Operation(summary = "新增acl规则", method = "POST")
    @PostMapping("/node/add")
    public ApiResponse<Void> addNode(@RequestBody @Validated FsAclNodeAddQuery query) {
        fsAclService.addNode(query);
        return ApiResponse.success();
    }

    @Log(title = "修改acl规则表", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:acl:list:edit')")
    @Operation(summary = "修改acl规则表", method = "POST")
    @PostMapping("/list/edit/{id}")
    public ApiResponse<Void> editList(@PathVariable("id") Long id, @RequestBody @Validated FsAclAddQuery query) {
        query.setId(id);
        fsAclService.editList(query);
        return ApiResponse.success();
    }

    @Log(title = "修改acl规则", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('system:acl:node:edit')")
    @Operation(summary = "修改acl规则", method = "POST")
    @PostMapping("/node/edit/{id}")
    public ApiResponse<Void> editNode(@PathVariable("id") Long id, @RequestBody @Validated FsAclNodeAddQuery query) {
        query.setId(id);
        fsAclService.editNode(query);
        return ApiResponse.success();
    }

    @Log(title = "acl规则详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:acl:get')")
    @Operation(summary = "acl详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<FsAcl> get(@PathVariable("id") Long id) {
        return ApiResponse.success(fsAclService.getDetail(id));
    }

    @Log(title = "删除acl", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('system:acl:delete')")
    @Operation(summary = "删除acl", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody FsAclQuery query) {
        fsAclService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "acl列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:acl:page:list')")
    @Operation(summary = "acl列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<FsAclVo>> pageList(@RequestBody FsAclQuery query) {
        PageInfo<FsAclVo> list = fsAclService.pageList(query);
        return ApiResponse.success(list);
    }

    @Log(title = "acl列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('system:acl:list')")
    @Operation(summary = "acl列表(不分页)", method = "POST")
    @PostMapping("/list")
    public ApiResponse<List<FsAclVo>> getList(@RequestBody FsAclQuery query) {
        List<FsAclVo> list = fsAclService.getList(query);
        return ApiResponse.success(list);
    }
}
