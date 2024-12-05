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
 * (Dispatcher) Kamailio负载均衡器
 *
 * @author danmo
 * @date 2024-07-29 10:49:18
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("ko_dispatcher")
public class KoDispatcher extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L; //1


    @Schema(description = "id")
    @TableId(type = IdType.AUTO)
    private Integer id;


    @Schema(description = "setid")
    @TableField("setid")
    private Integer setid;


    @Schema(description = "destination")
    @TableField("destination")
    private String destination;


    @Schema(description = "flags")
    @TableField("flags")
    private Integer flags;

    /**
     * 分配策略
     */
    @Schema(description = "分配策略")
    @TableField("priority")
    private Integer priority;


    @Schema(description = "attrs")
    @TableField("attrs")
    private String attrs;


    @Schema(description = "description")
    @TableField("description")
    private String description;

    /**
     * 状态 0-在线 1-下线
     */
    @Schema(description = "状态 0-在线 1-下线")
    @TableField("status")
    private Integer status;

}
