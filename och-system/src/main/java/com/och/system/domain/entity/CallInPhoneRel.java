package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 呼入号码路由关联实体
 * <p>映射 call_in_phone_rel 表，存储呼入号码与路由的关联关系。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("call_in_phone_rel")
public class CallInPhoneRel {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 呼入号码ID */
    @TableField("call_in_id")
    private Long callInId;

    /** 日程ID */
    @TableField("schedule_id")
    private Long scheduleId;

    /** 路由类型：1-坐席 2-外呼 3-sip 4-技能组 5-放音 6-ivr */
    @TableField("route_type")
    private Integer routeType;

    /** 路由类型值 */
    @TableField("route_value")
    private String routeValue;

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

