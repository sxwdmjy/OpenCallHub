package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * FS模块管理实体
 * <p>映射 fs_modules 表，存储 FreeSWITCH 模块配置。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("fs_modules")
public class FsModules {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** FS主机名 */
    @TableField("fs_name")
    private String fsName;

    /** 模块名称 */
    @TableField("name")
    private String name;

    /** 类型：xml格式 json格式 */
    @TableField("`type`")
    private String type;

    /** 内容 */
    @TableField("content")
    private String content;

    /** 描述 */
    @TableField("`describe`")
    private String describe;

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
