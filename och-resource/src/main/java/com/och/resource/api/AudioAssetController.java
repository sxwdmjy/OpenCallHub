package com.och.resource.api;

import lombok.RequiredArgsConstructor;

import com.och.common.api.ApiResponse;
import com.och.common.api.PageResponse;
import com.och.resource.service.IAudioAssetService;
import com.och.resource.domain.AudioAsset;
import com.och.security.audit.Audited;
import com.och.security.permission.RequiresPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 音频素材管理控制器，提供音频文件的CRUD操作。
 * 音频素材用于IVR语音导航、排队音乐、坐席提示等场景。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Tag(name = "AudioAsset", description = "音频素材管理")
@RestController
@RequestMapping("/api/resource/audio-assets")
@RequiredArgsConstructor
public class AudioAssetController {

    private final IAudioAssetService audioAssetService;

    @GetMapping
    public ApiResponse<PageResponse<AudioAsset>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(audioAssetService.getPageList(pageNum, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<AudioAsset> get(@PathVariable Long id) {
        return ApiResponse.success(audioAssetService.getDetail(id));
    }

    @PostMapping
    @Audited(action = "CREATE", targetType = "AUDIO_ASSET", description = "创建音频素材")
    @RequiresPermission(value = "resource:audio-asset:create", description = "创建音频素材")
    public ApiResponse<AudioAsset> create(@RequestBody AudioAsset asset) {
        return ApiResponse.success(audioAssetService.add(asset));
    }

    @PutMapping("/{id}")
    @Audited(action = "UPDATE", targetType = "AUDIO_ASSET", description = "更新音频素材")
    @RequiresPermission(value = "resource:audio-asset:update", description = "更新音频素材")
    public ApiResponse<AudioAsset> update(@PathVariable Long id, @RequestBody AudioAsset asset) {
        return ApiResponse.success(audioAssetService.edit(id, asset));
    }

    @DeleteMapping("/{id}")
    @Audited(action = "DELETE", targetType = "AUDIO_ASSET", description = "删除音频素材")
    @RequiresPermission(value = "resource:audio-asset:delete", description = "删除音频素材")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        audioAssetService.delete(id);
        return ApiResponse.success();
    }
}
