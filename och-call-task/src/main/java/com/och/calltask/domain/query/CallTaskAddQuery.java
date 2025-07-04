package com.och.calltask.domain.query;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 新增任务入参
 *
 * @author danmo
 * @date 2025/06/19 09:56
 */
@Schema(description = "新增任务入参")
@Data
public class CallTaskAddQuery {

    /**
     * 主键ID
     */

    @Schema(description = "主键ID", hidden = true)
    private Long id;


    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称")
    private String name;


    /**
     * 任务类型(0-预测 1-预览)
     */
    @NotNull(message = "任务类型不能为空")
    @Schema(description = "任务类型(0-预测 1-预览)")
    private Integer type;

    /**
     * 任务优先级
     */
    @Schema(description = "任务优先级")
    private Integer priority;


    /**
     * 任务开始时间
     */
    @NotNull(message = "任务开始时间不能为空")
    @Schema(description = "任务开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDay;


    /**
     * 任务结束时间
     */
    @NotNull(message = "任务结束时间不能为空")
    @Schema(description = "任务结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDay;


    /**
     * 每天开始时间
     */
    @NotBlank(message = "每天开始时间不能为空")
    @Schema(description = "每天开始时间")
    private String sTime;


    /**
     * 每天结束时间
     */
    @NotBlank(message = "每天结束时间不能为空")
    @Schema(description = "每天结束时间")
    private String eTime;


    /**
     * 周期时间
     */
    @NotBlank(message = "周期时间不能为空")
    @Schema(description = "周期时间")
    private String workCycle;


    /**
     * 数据源ID
     */
    @NotNull(message = "数据源ID不能为空")
    @Schema(description = "数据源ID")
    private Long sourceId;


    /**
     * 自动完成类型(0-是 1-否)
     */
    @NotNull(message = "自动完成类型不能为空")
    @Schema(description = "自动完成类型(0-是 1-否)")
    private Integer completeType;


    /**
     * 号码模式(0-轮询)
     */
    @NotNull(message = "号码模式不能为空")
    @Schema(description = "号码模式(0-轮询)")
    private Integer phoneMode;


    /**
     * 外显号码池
     */
    @NotNull(message = "外显号码池不能为空")
    @Schema(description = "外显号码池")
    private Long phonePoolId;


    /**
     * 转接类型(0-技能组 1-ivr 3-机器人)
     */
    @NotNull(message = "转接类型不能为空")
    @Schema(description = "转接类型(0-技能组 1-ivr 3-机器人)")
    private Integer transferType;


    /**
     * 转接类型值
     */
    @NotBlank(message = "转接类型值不能为空")
    @Schema(description = "转接类型值")
    private String transferValue;


    /**
     * 轮次配置
     */
    @NotBlank(message = "轮次配置不能为空")
    @Schema(description = "轮次配置")
    private String roundsConf;


    /**
     * 是否重呼(0-是 1-否)
     */
    @Schema(description = "是否重呼(0-是 1-否)")
    private Integer recall;


    /**
     * 重呼次数
     */
    @Schema(description = "重呼次数")
    private Integer recallNum;


    /**
     * 重呼间隔时长
     */
    @Schema(description = "重呼间隔时长（秒）")
    private Integer recallTime;


    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
