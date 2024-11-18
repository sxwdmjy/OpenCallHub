package com.och.system.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.och.common.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serializable;


/**
 * 文件管理(SysFile)表实体类
 *
 * @author danmo
 * @since 2024-11-18 10:22:33
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("sys_file")
public class SysFile extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 369002855331939265L;
   
    /**
     *  主键
     */

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Long id;


     
    /**
     *  云存储ID 
     */
    @Schema(description = "云存储ID")
    @TableField("cos_id")
    private String cosId;
    
    
     
    /**
     *  文件名称 
     */
    @Schema(description = "文件名称")
    @TableField("file_name")
    private String fileName;
    
    
     
    /**
     *  文件后缀 
     */
    @Schema(description = "文件后缀")
    @TableField("file_suffix")
    private String fileSuffix;
    
    
     
    /**
     *  文件类型 1-image 2-voice 3-file 
     */
    @Schema(description = "文件类型 1-image 2-voice 3-file")
    @TableField("file_type")
    private Integer fileType;
    
    
     
    /**
     *  文件地址 
     */
    @Schema(description = "文件地址")
    @TableField("file_path")
    private String filePath;
    
    
     
    /**
     *  文件大小 
     */
    @Schema(description = "文件大小")
    @TableField("file_size")
    private String fileSize;


}

