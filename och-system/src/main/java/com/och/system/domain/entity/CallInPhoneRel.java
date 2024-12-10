package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * 呼入号码路由子码表(CallInPhoneRel)表实体类
 *
 * @author danmo
 * @since 2024-12-10 10:29:11
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_in_phone_rel")
public class CallInPhoneRel extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 252659421962499048L;

    /**
     * 主键
     */

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 呼入号码ID
     */
    @Schema(description = "呼入号码ID")
    @TableField("call_in_id")
    private Long callInId;


    /**
     * 日程ID
     */
    @Schema(description = "日程ID")
    @TableField("schedule_id")
    private Long scheduleId;


    /**
     * 路由类型 1-坐席 2-外呼 3-sip 4-技能组 5-放音 6-ivr
     */
    @Schema(description = "路由类型 1-坐席 2-外呼 3-sip 4-技能组 5-放音 6-ivr")
    @TableField("route_type")
    private Integer routeType;


    /**
     * 路由类型值
     */
    @Schema(description = "路由类型值")
    @TableField("route_value")
    private String routeValue;


}

