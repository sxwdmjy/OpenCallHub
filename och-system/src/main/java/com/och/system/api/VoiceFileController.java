package com.och.system.api;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.api.ApiResponse;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.query.file.VoiceFileAddQuery;
import com.och.system.domain.query.file.VoiceFileQuery;
import com.och.system.domain.vo.file.VoiceFileVo;
import com.och.system.service.IVoiceFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 语音文件管理
 *
 * @author danmo
 */
@Slf4j
@Tag(name = "语音文件管理")
@RestController
@RequestMapping("/system/v1/voice")
@RequiredArgsConstructor
public class VoiceFileController {

    private final IVoiceFileService voiceFileService;

    @Log(title = "新增语音文件", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:voice:file:add')")
    @Operation(summary = "新增语音文件", method = "POST")
    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody @Validated VoiceFileAddQuery query) {
        voiceFileService.add(query);
        return ApiResponse.success();
    }

    @Log(title = "修改语音文件", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:voice:file:edit')")
    @Operation(summary = "修改语音文件", method = "POST")
    @PostMapping("/edit/{id}")
    public ApiResponse<Void> edit(@PathVariable("id") Long id, @RequestBody @Validated VoiceFileAddQuery query) {
        query.setId(id);
        voiceFileService.edit(query);
        return ApiResponse.success();
    }

    @Log(title = "删除语音文件", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:voice:file:delete')")
    @Operation(summary = "删除语音文件", method = "POST")
    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody VoiceFileQuery query) {
        voiceFileService.delete(query);
        return ApiResponse.success();
    }

    @Log(title = "语音文件详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:voice:file:get')")
    @Operation(summary = "语音文件详情", method = "POST")
    @PostMapping("/get/{id}")
    public ApiResponse<VoiceFileVo> getDetail(@PathVariable("id") Long id) {
        VoiceFileVo detail = voiceFileService.getDetail(id);
        return ApiResponse.success(detail);
    }

    @Log(title = "语音文件列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:voice:file:page:list')")
    @Operation(summary = "语音文件列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ApiResponse<PageInfo<VoiceFileVo>> pageList(@RequestBody VoiceFileQuery query) {
        List<VoiceFileVo> list = voiceFileService.getPageList(query);
        PageInfo<VoiceFileVo> pageInfo = new PageInfo<>(list);
        return ApiResponse.success(pageInfo);
    }

    @Operation(summary = "语音文件列表", method = "POST")
    @PostMapping("/list")
    public ApiResponse<List<VoiceFileVo>> getList(@RequestBody VoiceFileQuery query) {
        List<VoiceFileVo> list = voiceFileService.getList(query);
        return ApiResponse.success(list);
    }
}
