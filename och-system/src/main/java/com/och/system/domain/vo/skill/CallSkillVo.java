package com.och.system.domain.vo.skill;

import com.och.system.domain.vo.BaseVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author danmo
 * @date 2024-10-31 14:15
 **/
@EqualsAndHashCode(callSuper = true)
@Schema
@Data
public class CallSkillVo extends BaseVo {

    @Schema(description = "主键ID")
    private Long id;
    /**
     * 分组ID
     */
    @Schema(description = "分组ID")
    private Long groupId;

    @Schema(description = "分组名称")
    private String groupName;

    /**
     * 技能名称
     */
    @Schema(description = "技能名称")
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
    @Schema(description = "策略类型 0-随机 1-轮询 2-最长空闲时间 3-当天最少应答次数 4-最长话后时长")
    private Integer strategyType;


    /**
     * 全忙 0-排队 1-溢出 2-挂机
     */
    @Schema(description = "全忙 0-排队 1-溢出 2-挂机")
    private Integer fullBusyType;


    /**
     * 溢出策略 0-挂机 1-转IVR
     */
    @Schema(description = "溢出策略 0-挂机 1-转IVR")
    private Integer overflowType;


    /**
     * 溢出策略值
     */
    @Schema(description = "溢出策略值")
    private String overflowValue;


    /**
     * 排队超时时间（秒）
     */
    @Schema(description = "排队超时时间（秒）")
    private Integer timeOut;


    /**
     * 最大排队人数
     */
    @Schema(description = "最大排队人数")
    private Integer queueLength;


    /**
     * 排队音
     */
    @Schema(description = "排队音")
    private Long queueVoice;


    /**
     * 转坐席音
     */
    @Schema(description = "转坐席音")
    private Long agentVoice;

    @Schema(description = "主叫号码池")
    private Long callerPhonePool;

    @Schema(description = "被叫号码池")
    private Long calleePhonePool;


    @Schema(description = "坐席列表")
    private List<CallSkillAgentRelVo> agentList;
}
