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
 * 基于location_gaode手工整理后的表(用于匹配区号)(OchAreaCode)表实体类
 *
 * @author danmo
 * @since 2025-05-26 17:10:04
 */
@Schema
@Data
@SuppressWarnings("serial")
@TableName("och_area_code")
public class OchAreaCode  implements Serializable {
  private static final long serialVersionUID = 377995982711227256L;
   
    /**
     *  
     */

    @Schema(description = "")
    @TableId(type = IdType.AUTO)
    private Integer id;


     
    /**
     *  省份名称 
     */
    @Schema(description = "省份名称")
    @TableField("provinceName")
    private String provincename;
    
    
     
    /**
     *  城市名称 
     */
    @Schema(description = "城市名称")
    @TableField("cityName")
    private String cityname;
    
    
     
    /**
     *  省份地域编码 
     */
    @Schema(description = "省份地域编码")
    @TableField("provinceAdcode")
    private Integer provinceadcode;
    
    
     
    /**
     *  省份中心坐标 
     */
    @Schema(description = "省份中心坐标")
    @TableField("provinceCenter")
    private String provincecenter;
    
    
     
    /**
     *  城市区号 
     */
    @Schema(description = "城市区号")
    @TableField("cityCode")
    private String citycode;
    
    
     
    /**
     *  城市地域编码 
     */
    @Schema(description = "城市地域编码")
    @TableField("cityAdcode")
    private Integer cityadcode;
    
    
     
    /**
     *  城市中心坐标 
     */
    @Schema(description = "城市中心坐标")
    @TableField("cityCenter")
    private String citycenter;
    
    
     
    /**
     *  区域映射 
     */
    @Schema(description = "区域映射")
    @TableField("districts")
    private String districts;
    
    
     
    /**
     *  省份国际ISO编码 
     */
    @Schema(description = "省份国际ISO编码")
    @TableField("isoCode")
    private String isocode;
    
    


}

