package com.och.calltask.domain.query;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 呼叫任务联系人导入参数
 * @author danmo
 * @date 2025/06/19 10:10
 */
@Schema(description = "呼叫任务联系人导入参数")
@Data
public class CallTaskContactImportQuery  {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "导入方式 0-人群导入 1-文件导入")
    private Integer importType;

    @Schema(description = "人群ID")
    private Long crowdId;

    @Schema(description = "模板ID")
    private Long templateId;
}
