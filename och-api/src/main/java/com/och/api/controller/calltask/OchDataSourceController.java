package com.och.api.controller.calltask;


import com.github.pagehelper.PageInfo;
import com.och.common.annotation.Log;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.enums.BusinessTypeEnum;
import com.och.system.domain.query.calltask.DataSourceAddQuery;
import com.och.system.domain.query.calltask.DataSourceContactQuery;
import com.och.system.domain.query.calltask.DataSourceQuery;
import com.och.system.domain.vo.calltask.DataSourceVo;
import com.och.system.domain.vo.calltask.DataSourcesContactVo;
import com.och.system.service.IOchDataSourcesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 数据源管理
 *
 * @author danmo
 * @date 2025/06/16 14:59
 */
@Tag(name = "数据源管理")
@RestController
@RequestMapping("/call/task/v1/source")
public class OchDataSourceController extends BaseController {

    @Autowired
    private IOchDataSourcesService ochDataSourcesService;


    @Log(title = "新增数据源", businessType = BusinessTypeEnum.INSERT)
    @PreAuthorize("@authz.hasPerm('call:task:source:add')")
    @Operation(summary = "新增数据源", method = "POST")
    @PostMapping("/add")
    public ResResult add(@RequestBody @Validated DataSourceAddQuery query) {
        ochDataSourcesService.add(query);
        return success();
    }

    @Log(title = "修改数据源", businessType = BusinessTypeEnum.UPDATE)
    @PreAuthorize("@authz.hasPerm('call:task:source:edit')")
    @Operation(summary = "修改数据源", method = "POST")
    @PostMapping("/edit/{id}")
    public ResResult edit(@PathVariable("id") Long id, @RequestBody @Validated DataSourceAddQuery query) {
        query.setId(id);
        ochDataSourcesService.edit(query);
        return success();
    }

    @Log(title = "数据源详情", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:task:source:get')")
    @Operation(summary = "数据源详情", method = "POST")
    @PostMapping("/get/{id}")
    public ResResult<DataSourceVo> get(@PathVariable("id") Long id) {
        return success(ochDataSourcesService.get(id));
    }


    @Log(title = "删除数据源", businessType = BusinessTypeEnum.DELETE)
    @PreAuthorize("@authz.hasPerm('call:task:source:delete')")
    @Operation(summary = "删除数据源", method = "POST")
    @PostMapping("/delete")
    public ResResult delete(@RequestBody DataSourceQuery query) {
        ochDataSourcesService.delete(query);
        return success();
    }

    @Log(title = "数据源列表(分页)", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:task:source:page:list')")
    @Operation(summary = "数据源列表(分页)", method = "POST")
    @PostMapping("/page/list")
    public ResResult<PageInfo<DataSourceVo>> pageList(@RequestBody DataSourceQuery query) {
        List<DataSourceVo> list = ochDataSourcesService.pageList(query);
        return success(new PageInfo<>(list));
    }

    @Log(title = "数据源列表(不分页)", businessType = BusinessTypeEnum.SELECT)
    @Operation(summary = "数据源列表(不分页)", method = "POST")
    @PreAuthorize("@authz.hasPerm('call:task:source:list')")
    @PostMapping("/list")
    public ResResult<List<DataSourceVo>> list(@RequestBody DataSourceQuery query) {
        List<DataSourceVo> list = ochDataSourcesService.getList(query);
        return success(list);
    }

    @Log(title = "数据源联系人列表（分页）", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:task:source:contact:page:list')")
    @Operation(summary = "数据源联系人列表（分页）", method = "POST")
    @PostMapping("/contact/page/list")
    public ResResult<PageInfo<List<DataSourcesContactVo>>> contactPageList(@RequestBody DataSourceContactQuery query) {
        List<DataSourcesContactVo> list = ochDataSourcesService.getContactPageList(query);
        return success(new PageInfo<>(list));
    }

    @Log(title = "数据源联系人列表（不分页）", businessType = BusinessTypeEnum.SELECT)
    @PreAuthorize("@authz.hasPerm('call:task:source:contact:list')")
    @Operation(summary = "数据源联系人列表（不分页）", method = "POST")
    @PostMapping("/contact/list")
    public ResResult<List<DataSourcesContactVo>> contactList(@RequestBody DataSourceContactQuery query) {
        List<DataSourcesContactVo> list = ochDataSourcesService.getContactList(query);
        return success(list);
    }

    @Log(title = "联系人模板下载", businessType = BusinessTypeEnum.OTHER)
    @PreAuthorize("@authz.hasPerm('call:task:source:contact:template:download')")
    @Operation(summary = "联系人模板下载", method = "POST")
    @PostMapping("/contact/template/download/{sourceId}")
    public void contactTemplateDownload(@PathVariable("sourceId") Long sourceId, HttpServletResponse response) {
        ochDataSourcesService.contactTemplateDownload(sourceId,  response);
    }

    @Log(title = "数据源联系人模板上传", businessType = BusinessTypeEnum.OTHER)
    @PreAuthorize("@authz.hasPerm('call:task:source:contact:template:upload')")
    @Operation(summary = "数据源联系人模板上传", method = "POST")
    @PostMapping("/contact/template/upload/{sourceId}")
    public ResResult contactTemplateUpload(@PathVariable("sourceId") Long sourceId, @RequestParam("file") MultipartFile file) {
        ochDataSourcesService.contactTemplateUpload(sourceId, file);
        success();
    }
}
