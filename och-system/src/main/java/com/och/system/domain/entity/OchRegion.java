package com.och.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


/**
 * (OchRegion)表实体类
 *
 * @author danmo
 * @since 2025-05-26 17:11:09
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("och_region")
public class OchRegion implements Serializable {
    private static final long serialVersionUID = 334462314758656225L;


    /**
     * 行政代码
     */
    @Schema(description = "行政代码")
    @TableId("region_id")
    private String regionId;


    /**
     * 名称
     */
    @Schema(description = "名称")
    @TableField("region_name")
    private String regionName;


    /**
     * 行政代码
     */
    @Schema(description = "行政代码")
    @TableField("parent_id")
    private String parentId;


    /**
     *
     */
    @Schema(description = "")
    @TableField("short_name")
    private String shortName;


    /**
     * 层级
     */
    @Schema(description = "层级")
    @TableField("level")
    private String level;


    /**
     * 区号
     */
    @Schema(description = "区号")
    @TableField("city_code")
    private String cityCode;


    /**
     * 邮政编码
     */
    @Schema(description = "邮政编码")
    @TableField("zip_code")
    private String zipCode;


    /**
     *
     */
    @Schema(description = "")
    @TableField("merger_name")
    private String mergerName;


    /**
     * 经度
     */
    @Schema(description = "经度")
    @TableField("lng")
    private String lng;


    /**
     * 纬度
     */
    @Schema(description = "纬度")
    @TableField("lat")
    private String lat;


    /**
     * 拼音
     */
    @Schema(description = "拼音")
    @TableField("pinyin")
    private String pinyin;


}

