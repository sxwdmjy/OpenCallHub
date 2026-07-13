package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 文件管理实体
 * <p>映射 sys_file 表，存储系统文件信息。</p>
 *
 * @author danmo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_file")
public class SysFile {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;


    /** 云存储ID */
    @TableField("cos_id")
    private String cosId;

    /** 文件名称 */
    @TableField("file_name")
    private String fileName;

    /** 文件后缀 */
    @TableField("file_suffix")
    private String fileSuffix;

    /** 文件类型：1-image 2-voice 3-file */
    @TableField("file_type")
    private Integer fileType;

    /** 文件地址 */
    @TableField("file_path")
    private String filePath;

    /** 文件大小 */
    @TableField("file_size")
    private String fileSize;

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

