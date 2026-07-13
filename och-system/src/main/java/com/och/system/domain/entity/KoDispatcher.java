package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * Kamailio 负载均衡调度器
 * <p>映射 ko_dispatcher 表，存储 Kamailio 的 dispatcher 模块配置，
 * 包含目标地址、分配策略、状态等负载均衡参数。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ko_dispatcher")
public class KoDispatcher {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 集合ID */
    @TableField("setid")
    private Integer setid;

    /** 目标地址（SIP URI） */
    @TableField("destination")
    private String destination;

    /** 标志位 */
    @TableField("flags")
    private Integer flags;

    /** 分配策略优先级 */
    @TableField("priority")
    private Integer priority;

    /** 属性描述 */
    @TableField("attrs")
    private String attrs;

    /** 描述 */
    @TableField("description")
    private String description;

    /** 状态：0-在线 1-下线 */
    @TableField("status")
    private Integer status;

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
