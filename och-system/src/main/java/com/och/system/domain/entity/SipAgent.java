package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 坐席管理实体
 * <p>映射 sip_agent 表，存储坐席信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sip_agent")
public class SipAgent {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 坐席名称 */
    @TableField("name")
    private String name;

    /** 员工ID */
    @TableField("user_id")
    private Long userId;

    /** SIP账号 */
    @TableField("agent_number")
    private String agentNumber;

    /** 状态：0-未开通 1-开通 */
    @TableField("status")
    private Integer status;

    /** 在线状态：1-空闲 2-忙碌 3-勿扰 4-离线 5-通话中 6-振铃中 7-话后 */
    @TableField("online_status")
    private Integer onlineStatus;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新人 */
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;

    /** 更新时间 */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /** 逻辑删除：0-正常 1-已删除 */
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;

}
