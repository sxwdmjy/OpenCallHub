package com.och.resource.api;

import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.resource.domain.entity.FsNode;
import com.och.resource.service.IFsNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FreeSWITCH 节点管理
 * <p>管理 FreeSWITCH 服务器节点的连接信息，包括节点地址、端口、认证密码。
 * 节点信息用于系统启动时自动建立 ESL 连接，管理员可在线增删节点无需重启。</p>
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "FS节点管理")
@RestController
@RequestMapping("/fs/v1/node")
@RequiredArgsConstructor
public class FsNodeController {

    private final IFsNodeService fsNodeService;

    /**
     * 查询所有节点（启用的排前面）
     */
    @Operation(summary = "节点列表", method = "GET")
    @GetMapping("/list")
    public ApiResponse<List<FsNode>> list() {
        return ApiResponse.success(fsNodeService.getAllNodes());
    }

    /**
     * 节点详情
     */
    @Operation(summary = "节点详情", method = "GET")
    @GetMapping("/{id}")
    public ApiResponse<FsNode> getDetail(@PathVariable Long id) {
        return ApiResponse.success(fsNodeService.getById(id));
    }

    /**
     * 新增节点
     */
    @Log(title = "新增FS节点", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('fs:node:add')")
    @Operation(summary = "新增节点", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated FsNode node) {
        fsNodeService.add(node);
        return ApiResponse.success();
    }

    /**
     * 修改节点
     */
    @Log(title = "修改FS节点", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('fs:node:edit')")
    @Operation(summary = "修改节点", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable Long id, @RequestBody @Validated FsNode node) {
        node.setId(id);
        fsNodeService.update(node);
        return ApiResponse.success();
    }

    /**
     * 删除节点
     */
    @Log(title = "删除FS节点", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('fs:node:delete')")
    @Operation(summary = "删除节点", method = "POST")
    @PostMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fsNodeService.delete(id);
        return ApiResponse.success();
    }
}
