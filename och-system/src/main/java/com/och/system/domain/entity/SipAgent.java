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
 * 坐席管理表(SysAgent)
 *
 * @author danmo
 * @date 2023-09-26 11:08:58
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("sip_agent")
public class SipAgent extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L; //1


    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "坐席名称")
    @TableField("name")
    private String name;

    @Schema(description = "员工ID")
    @TableField("user_id")
    private Long userId;


    @Schema(description = "sip账号")
    @TableField("agent_number")
    private String agentNumber;

    @Schema(description = "状态 0-未开通 1-开通")
    @TableField("status")
    private Integer status;

    /**
     * 在线状态 1-空闲 2-忙碌 3-勿扰 4-离线 5-通话中 6-振铃中 7-话后
     */
    @Schema(description = "在线状态 1-空闲 2-忙碌 3-勿扰 4-离线 5-通话中 6-振铃中 7-话后")
    @TableField("online_status")
    private Integer onlineStatus;
}
