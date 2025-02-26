package com.och.api.controller;

import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.domain.file.FileUploadVo;
import com.och.system.service.ISysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/system/v1/file")
public class SysFileController extends BaseController {

    @Autowired
    private ISysFileService sysFileService;

    /**
     * 文件上传
     *
     * @param file 文件
     * @param type 上传方式 local ali  tx
     * @return 文件上传结果
     */
    @Operation(description = "文件上传", method = "POST")
    @PostMapping("/upload")
    public ResResult<FileUploadVo> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        FileUploadVo uploadedFile = sysFileService.uploadFile(file,type);
        return success(uploadedFile);
    }


}
