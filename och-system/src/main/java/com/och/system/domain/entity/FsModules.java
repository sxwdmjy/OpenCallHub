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
 * fs模块管理表(FsModules)
 *
 * @author danmo
 * @date 2023-09-15 15:42:28
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("fs_modules")
public class FsModules extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L; //1

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * FS主机名
     */
    @Schema(description = "FS主机名")
    @TableField("fs_name")
    private String fsName;


    /**
     * 模块名称
     */
    @Schema(description = "模块名称")
    @TableField("name")
    private String name;


    /**
     * 类型 xml格式,json格式
     */
    @Schema(description = "类型 xml格式,json格式")
    @TableField("`type`")
    private String type;


    /**
     * 内容
     */
    @Schema(description = "内容")
    @TableField("content")
    private String content;


    /**
     * 描述
     */
    @Schema(description = "描述")
    @TableField("`describe`")
    private String describe;


}
