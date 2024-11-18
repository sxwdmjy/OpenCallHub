package com.och.common.domain.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author danmo
 * @date 2023-11-02 14:09
 **/
@Schema
@Data
public class FileUploadVo {

    @Schema(description = "文件ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @Schema(description = "文件ID")
    private String cosId;

    @Schema(description = "文件名称")
    private String fileName;

    @Schema(description = "文件地址")
    private String filePath;

    @Schema(description = "文件大小")
    private String fileSize;

    @Schema(description = "文件后缀")
    private String fileSuffix;

    @Schema(description = "文件类型 1-image 2-file 3-voice")
    private Integer fileType;
}
