package com.och.api.controller.call;

import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.query.file.VoiceFileAddQuery;
import com.och.system.domain.query.file.VoiceFileQuery;
import com.och.system.domain.vo.file.VoiceFileVo;
import com.och.system.service.IVoiceFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author danmo
 * @date 2023-11-02 11:15
 **/
@Tag(name = "语音文件管理")
@RestController
@RequestMapping("call/v1/voice/file")
public class VoiceFileController extends BaseController {

    @Autowired
    private IVoiceFileService iVoiceFileService;

    @Log(title = "新增语音文件", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:voice:file:add')")
    @Operation(summary = "新增语音文件", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated VoiceFileAddQuery query) {
        iVoiceFileService.add(query);
        return ResResult.success();
    }

    @Log(title = "修改语音文件", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:voice:file:edit')")
    @Operation(summary = "修改语音文件", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated VoiceFileAddQuery query) {
        query.setId(id);
        iVoiceFileService.edit(query);
        return ResResult.success();
    }

    @Log(title = "删除语音文件", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:voice:file:delete')")
    @Operation(summary = "删除语音文件", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody VoiceFileQuery query) {
        iVoiceFileService.delete(query);
        return ResResult.success();
    }

    @Log(title = "语音文件详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:voice:file:get')")
    @Operation(summary = "语音文件详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<VoiceFileVo> getDetail(@PathVariable("id") Long id) {
        VoiceFileVo detail = iVoiceFileService.getDetail(id);
        return ResResult.success(detail);
    }

    @Log(title = "语音文件列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:voice:file:page:list')")
    @Operation(summary = "语音文件列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<VoiceFileVo>> pageList(@RequestBody VoiceFileQuery query) {
        List<VoiceFileVo> list = iVoiceFileService.getPageList(query);
        PageInfo<VoiceFileVo> pageInfo = new PageInfo<>(list);
        return success(pageInfo);
    }

    @Operation(summary = "语音文件列表", method = "POST")
    @PostMapping("/list")
    public ResResult<List<VoiceFileVo>> getList(@RequestBody VoiceFileQuery query) {
        List<VoiceFileVo> list = iVoiceFileService.getList(query);
        return success(list);
    }
}
