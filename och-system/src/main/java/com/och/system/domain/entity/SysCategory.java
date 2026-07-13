package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;


/**
 * 分类配置实体
 * <p>映射 sys_category 表，存储系统分类配置信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_category")
public class SysCategory {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;


    /** 类型：1-技能 */
    @TableField("type")
    private Integer type;

    /** 分类名称 */
    @TableField("name")
    private String name;

    /** 父分类ID */
    @TableField("parent_id")
    private Long parentId;

    /** 可删除标识：0-可删除 1-不可删除 */
    @TableField("flag")
    private Integer flag;

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

