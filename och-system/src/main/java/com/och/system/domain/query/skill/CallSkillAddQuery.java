package com.och.system.domain.query.skill;

import com.och.system.domain.entity.CallSkillAgentRel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author danmo
 * @date 2023-10-31 14:15
 **/
@Schema
@Data
public class CallSkillAddQuery {

    @Schema(description = "主键ID", hidden = true)
    private Long id;

    /**
     * 分组ID
     */
    @NotNull(message = "分组ID不能为空")
    @Schema(description = "分组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long groupId;


    /**
     * 技能名称
     */
    @NotEmpty(message = "技能名称不能为空")
    @Schema(description = "技能名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 描述
     */
    @Schema(description = "描述")
    private String describe;

    /**
     * 优先级
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 策略类型 0-随机 1-轮询 2-最长空闲时间 3-当天最少应答次数 4-最长话后时长
     */
    @Schema(description = "策略类型 0-随机 1-轮询 2-最长空闲时间 3-当天最少应答次数 4-最长话后时长", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer strategyType;


    /**
     * 全忙 0-排队 1-溢出 2-挂机
     */
    @Schema(description = "全忙 0-排队 1-溢出 2-挂机", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer fullBusyType;


    /**
     * 溢出策略 0-挂机 1-转IVR
     */
    @Schema(description = "溢出策略 0-挂机 1-转IVR", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer overflowType;


    /**
     * 溢出策略值
     */
    @Schema(description = "溢出策略值")
    private String overflowValue;


    /**
     * 排队超时时间（秒）
     */
    @Schema(description = "排队超时时间（秒）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer timeOut;


    /**
     * 最大排队人数
     */
    @Schema(description = "最大排队人数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer queueLength;


    /**
     * 排队音
     */
    @Schema(description = "排队音")
    private Long queueVoice;


    /**
     * 排队音
     */
    @Schema(description = "排队音")
    private String queueVoiceValue;


    /**
     * 转坐席音
     */
    @Schema(description = "转坐席音")
    private Long agentVoice;


    /**
     * 转坐席音
     */
    @Schema(description = "转坐席音")
    private String agentVoiceValue;


    @Schema(description = "坐席列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CallSkillAgentRel> agentList;
}
