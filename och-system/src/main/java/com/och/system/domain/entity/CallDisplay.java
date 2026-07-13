package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 号码管理实体
 * <p>映射 call_display 表，存储号码信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("call_display")
public class CallDisplay {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 电话号码 */
    @TableField("phone")
    private String phone;

    /** 号码类型：1-主叫显号 2-被叫显号 */
    @TableField("type")
    private Integer type;

    /** 归属地 */
    @TableField("area")
    private String area;

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

