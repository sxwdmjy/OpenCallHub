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
 * 号码路由表(CallRoute)表实体类
 *
 * @author danmo
 * @since 2024-12-30 14:03:42
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("call_route")
public class CallRoute extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -77948255330539169L;

    /**
     *
     */

    @Schema(description = "")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 路由名称
     */
    @Schema(description = "路由名称")
    @TableField("name")
    private String name;


    /**
     * 路由号码
     */
    @Schema(description = "路由号码")
    @TableField("route_num")
    private String routeNum;


    /**
     * 路由类型 1-呼入 2-呼出
     */
    @Schema(description = "路由类型 1-呼入 2-呼出")
    @TableField("type")
    private Integer type;


    /**
     * 路由优先级
     */
    @Schema(description = "路由优先级")
    @TableField("level")
    private Integer level;


    /**
     * 状态  0-未启用 1-启用
     */
    @Schema(description = "状态  0-未启用 1-启用")
    @TableField("status")
    private Integer status;

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

