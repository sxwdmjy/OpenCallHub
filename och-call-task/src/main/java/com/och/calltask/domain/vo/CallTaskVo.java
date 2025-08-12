package com.och.calltask.domain.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.och.system.domain.vo.BaseVo;
import com.och.system.domain.vo.agent.SipSimpleAgent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 呼叫任务返回参数
 * @author danmo
 * @date 2025/06/19 10:19
 */
@Schema(description = "呼叫任务返回参数")
@EqualsAndHashCode(callSuper = true)
@Data
public class CallTaskVo extends BaseVo {

    /**
     *  主键ID
     */

    @Schema(description = "主键ID")
    private Long id;


    /**
     *  任务名称
     */
    @Schema(description = "任务名称")
    private String name;



    /**
     *  任务类型(0-预测 1-预览)
     */
    @Schema(description = "任务类型(0-预测 1-预览)")
    private Integer type;



    /**
     *  任务状态(0-未开始 1-进行中 2-暂停 3-结束)
     */
    @Schema(description = "任务状态(0-未开始 1-进行中 2-暂停 3-结束)")
    private Integer status;



    /**
     *  任务优先级
     */
    @Schema(description = "任务优先级")
    private Integer priority;



    /**
     *  任务开始时间
     */
    @Schema(description = "任务开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date startDay;



    /**
     *  任务结束时间
     */
    @Schema(description = "任务结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd",timezone = "GMT+8")
    private Date endDay;



    /**
     *  每天开始时间
     */
    @Schema(description = "每天开始时间")
    private String sTime;



    /**
     *  每天结束时间
     */
    @Schema(description = "每天结束时间")
    private String eTime;



    /**
     *  周期时间
     */
    @Schema(description = "周期时间")
    private String workCycle;


    /**
     *  分配方式 1-轮流 2-空闲
     */
    @Schema(description = "分配方式 1-轮流 2-空闲")
    private Integer assignmentType;



    /**
     *  是否有限分配: 1-否 2-是
     */
    @Schema(description = "是否有限分配: 1-否 2-是")
    private Integer isPriority;



    /**
     *  接待个数限制
     */
    @Schema(description = "接待个数限制")
    private Integer receiveLimit;



    /**
     *  执行坐席列表（为空全部坐席）
     */
    @Schema(description = "执行坐席列表（为空全部坐席）")
    private List<SipSimpleAgent> agentList;

    /**
     *  自动完成类型(0-是 1-否)
     */
    @Schema(description = "自动完成类型(0-是 1-否)")
    private Integer completeType;


    /**
     *  外显号码池
     */
    @Schema(description = "外显号码池")
    private Long phonePoolId;

    @Schema(description = "外显号码池名称")
    private String phonePoolName;



    /**
     *  转接类型(0-技能组 1-ivr 3-机器人)
     */
    @Schema(description = "转接类型(0-技能组 1-ivr 3-机器人)")
    private Integer transferType;


    /**
     *  转接类型值
     */
    @Schema(description = "转接类型值")
    private String transferValue;


    @Schema(description = "转接类型值名称")
    private String transferValueName;


    /**
     *  是否重呼(0-是 1-否)
     */
    @Schema(description = "是否重呼(0-是 1-否)")
    private Integer recall;



    /**
     *  重呼次数
     */
    @Schema(description = "重呼次数")
    private Integer recallNum;



    /**
     *  重呼间隔时长
     */
    @Schema(description = "重呼间隔时长（秒）")
    private Integer recallTime;



    /**
     *  备注
     */
    @Schema(description = "备注")
    private String remark;

}
