package com.och.calltask.domain.query;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.och.system.domain.query.BaseQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 呼叫任务查询参数
 * @author danmo
 * @date 2025/06/19 10:10
 */
@Schema(description = "呼叫任务查询参数")
@EqualsAndHashCode(callSuper = true)
@Data
public class CallTaskQuery extends BaseQuery {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "主键ID集合")
    private List<Long> idList;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "任务类型(0-预测 1-预览)")
    private Integer type;

    @Schema(description = "任务优先级")
    private Integer priority;

    @Schema(description = "任务开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startDay;

    @Schema(description = "任务结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endDay;

    @Schema(description = "数据源ID")
    private Long sourceId;

    @Schema(description = "自动完成类型(0-是 1-否)")
    private Integer completeType;

    @Schema(description = "转接类型(0-技能组 1-ivr 3-机器人)")
    private Integer transferType;

}
