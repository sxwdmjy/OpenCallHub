package com.och.resource.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.resource.service.IAiPluginResourceService;
import com.och.resource.domain.AiPlugin;
import com.och.security.audit.Audited;
import com.och.security.permission.RequiresPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * AI插件资源管理控制器，提供AI引擎插件的CRUD操作。
 * 支持ASR、TTS等AI引擎的配置管理，自动脱敏apiKey/apiSecret。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Tag(name = "AiPlugin", description = "AI插件资源管理")
@RestController("resourceAiPluginController")
@RequestMapping("/api/resource/ai-plugins")
@RequiredArgsConstructor
public class AiPluginController {

    private final IAiPluginResourceService aiPluginResourceService;

    @GetMapping
    public ApiResponse<PageResponse<AiPlugin>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String pluginType,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(aiPluginResourceService.getPageList(pageNum, pageSize, pluginType, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<AiPlugin> get(@PathVariable Long id) {
        return ApiResponse.success(aiPluginResourceService.getDetail(id));
    }

    @PostMapping
    @Audited(action = "CREATE", targetType = "AI_PLUGIN", description = "创建AI插件")
    @RequiresPermission(value = "resource:ai-plugin:create", description = "创建AI插件")
    public ApiResponse<AiPlugin> create(@RequestBody AiPlugin plugin) {
        return ApiResponse.success(aiPluginResourceService.add(plugin));
    }

    @PutMapping("/{id}")
    @Audited(action = "UPDATE", targetType = "AI_PLUGIN", description = "更新AI插件")
    @RequiresPermission(value = "resource:ai-plugin:update", description = "更新AI插件")
    public ApiResponse<AiPlugin> update(@PathVariable Long id, @RequestBody AiPlugin plugin) {
        return ApiResponse.success(aiPluginResourceService.edit(id, plugin));
    }

    @DeleteMapping("/{id}")
    @Audited(action = "DELETE", targetType = "AI_PLUGIN", description = "删除AI插件")
    @RequiresPermission(value = "resource:ai-plugin:delete", description = "删除AI插件")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        aiPluginResourceService.delete(id);
        return ApiResponse.success();
    }
}
